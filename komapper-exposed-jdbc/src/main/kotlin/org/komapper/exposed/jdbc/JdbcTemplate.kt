package org.komapper.exposed.jdbc

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.komapper.core.BuilderDialect
import org.komapper.core.DataType
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.Value
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.exposed.Argument
import org.komapper.exposed.resolveColumnType
import org.komapper.template.TwoWayTemplateStatementBuilderFactory
import java.sql.ResultSet
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Interface for executing SQL templates with JDBC.
 *
 * This interface allows executing raw SQL queries with bound parameters
 * using Exposed's transaction management system.
 */
interface JdbcTemplate {
    /**
     * Executes the SQL template and transforms the result set.
     *
     * @param T the return type
     * @param transform a function to transform the [ResultSet] into the desired result type
     * @return the transformed result, or null if the result set is empty
     */
    fun <T> execute(transform: (ResultSet) -> T?): T?
}

/**
 * Builder class for constructing JDBC SQL templates.
 *
 * This builder collects SQL template strings and bound parameters,
 * which are then used to create a [JdbcTemplate] instance.
 */
class JdbcTemplateBuilder() {
    @PublishedApi
    internal val nameToValueMap: MutableMap<String, Value<*>> = mutableMapOf()

    @PublishedApi
    internal var rawTemplate: String? = null

    /**
     * Sets the SQL template string.
     *
     * @param rawTemplate the SQL template with parameter placeholders (e.g., `/*name*/`)
     */
    fun build(
        @Language("sql") rawTemplate: String,
    ) {
        this.rawTemplate = rawTemplate
    }
}

/**
 * Creates a JDBC SQL template with bound parameters.
 *
 * This function provides a DSL for constructing SQL templates with type-safe parameter binding.
 * Parameters are bound using delegated properties with the [arg] function, and the SQL template
 * is specified using the [JdbcTemplateBuilder.build] function.
 *
 * Example usage:
 * ```kotlin
 * val template = jdbcTemplate {
 *     val id by arg(1, IntegerColumnType())
 *     val name by arg("John", VarCharColumnType())
 *     build("SELECT * FROM users WHERE id = /*id*/0 AND name = /*name*/''")
 * }
 * val result = template.execute { rs ->
 *     // Transform ResultSet
 * }
 * ```
 *
 * @param block the builder configuration block
 * @return a [JdbcTemplate] instance ready for execution
 * @throws IllegalStateException if the [JdbcTemplateBuilder.build] method is not called
 */
fun jdbcTemplate(block: JdbcTemplateBuilder.() -> Unit): JdbcTemplate {
    val builder = JdbcTemplateBuilder()
    builder.block()
    val rawTemplate = builder.rawTemplate ?: error("The invocation of the build method is missing.")
    return JdbcTemplateImpl(rawTemplate, builder.nameToValueMap.toMap())
}

/**
 * Binds a value to a SQL template parameter using an Exposed column definition.
 *
 * This function creates a delegated property that binds the given value to a parameter
 * in the SQL template. The parameter name is derived from the property name.
 * The column type is extracted from the provided Exposed [Column] and configured to accept null values.
 *
 * @param T the type of the value
 * @param value the value to bind (nullable)
 * @param column the Exposed column definition that provides type information
 * @return a read-only property delegate that produces an [Argument]
 */
inline fun <reified T : Any> JdbcTemplateBuilder.arg(value: T?, column: Column<T>): ReadOnlyProperty<Nothing?, Argument<T>> {
    // Create a new instance to ensure that the original nullable property remains unchanged when calling `TransactionManager.current().exec()`.
    val columnType = object : IColumnType<T> by column.columnType {
        override var nullable: Boolean = true
    }
    return bind(value, typeOf<T>(), columnType)
}

/**
 * Binds a value to a SQL template parameter using an explicit column type.
 *
 * This function creates a delegated property that binds the given value to a parameter
 * in the SQL template. The parameter name is derived from the property name.
 *
 * @param T the type of the value
 * @param value the value to bind (nullable)
 * @param columnType the Exposed column type for proper JDBC binding
 * @return a read-only property delegate that produces an [Argument]
 */
inline fun <reified T : Any> JdbcTemplateBuilder.arg(value: T?, columnType: IColumnType<T>): ReadOnlyProperty<Nothing?, Argument<T>> {
    return bind(value, typeOf<T>(), columnType)
}

/**
 * Internal function to bind a value to a SQL template parameter.
 *
 * This function creates a delegated property that registers the parameter
 * in the builder's value map and returns an [Argument] instance.
 *
 * @param T the type of the value
 * @param value the value to bind (nullable)
 * @param type the Kotlin type information
 * @param columnType the Exposed column type
 * @return a read-only property delegate that produces an [Argument]
 */
@PublishedApi
internal fun <T : Any> JdbcTemplateBuilder.bind(value: T?, type: KType, columnType: IColumnType<T>): ReadOnlyProperty<Nothing?, Argument<T>> {
    return ReadOnlyProperty<Nothing?, Argument<T>> { _, property ->
        val name = property.name
        nameToValueMap[name] = Value(value, type, hint = columnType)
        Argument(name, value, type, columnType)
    }
}

/**
 * Internal implementation of [JdbcTemplate].
 *
 * This class handles the actual SQL template processing and execution:
 * 1. Builds the SQL statement from the template using two-way SQL binding
 * 2. Converts parameters to Exposed column types
 * 3. Executes the query through Exposed's transaction manager
 *
 * @param sql the SQL template string
 * @param nameToValueMap the map of parameter names to their bound values
 */
internal class JdbcTemplateImpl(private val sql: String, private val nameToValueMap: Map<String, Value<*>>) : JdbcTemplate {
    override fun <T> execute(transform: (ResultSet) -> T?): T? {
        val dialect = JdbcTemplateDialect()
        val builder = TwoWayTemplateStatementBuilderFactory().create(dialect)
        val extensions = TemplateBuiltinExtensions { dialect.escape(it) }
        val statement = builder.build(sql, nameToValueMap, extensions)
        val sql = statement.toSql(dialect::createBindVariable)
        val args = statement.args.map {
            val columnType = it.hint as? IColumnType<*> ?: resolveColumnType(it.type)
            columnType to it.any
        }
        return TransactionManager.current().exec(stmt = sql, args = args, transform = transform)
    }
}

/**
 * Internal dialect implementation for JDBC SQL template processing.
 *
 * This dialect provides minimal functionality required for SQL template building
 * and parameter binding. It is not a full-featured Komapper dialect and only
 * supports operations needed for template processing.
 *
 * Most methods throw [UnsupportedOperationException] as they are not required
 * for SQL template functionality.
 */
class JdbcTemplateDialect() : BuilderDialect {
    override val driver: String = "komapper-exposed-jdbc"

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
        throw UnsupportedOperationException()
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> formatValue(
        value: T?,
        type: KType,
        masking: Boolean,
    ): String {
        return value.toString()
    }

    override fun <T : Any> getDataTypeName(type: KType): String {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> getDataType(type: KType): DataType {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> getDataTypeOrNull(type: KType): DataType {
        throw UnsupportedOperationException()
    }
}
