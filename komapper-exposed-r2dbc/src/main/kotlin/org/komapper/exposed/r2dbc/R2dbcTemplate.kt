package org.komapper.exposed.r2dbc

import io.r2dbc.spi.R2dbcException
import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
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
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.template.TwoWayTemplateStatementBuilderFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface R2dbcTemplate {
    suspend fun <T> execute(transform: (Row) -> T?): Flow<T?>?
}

class R2dbcTemplateBuilder() {
    @PublishedApi
    internal val nameToValueMap: MutableMap<String, Value<*>> = mutableMapOf()

    @PublishedApi
    internal var rawTemplate: String? = null

    fun build(
        @Language("sql") rawTemplate: String,
    ) {
        this.rawTemplate = rawTemplate
    }
}

fun r2dbcTemplate(block: R2dbcTemplateBuilder.() -> Unit): R2dbcTemplate {
    val builder = R2dbcTemplateBuilder()
    builder.block()
    val rawTemplate = builder.rawTemplate ?: error("The invocation of the build method is missing.")
    return R2dbcTemplateImpl(rawTemplate, builder.nameToValueMap.toMap())
}

inline fun <reified T : Any> R2dbcTemplateBuilder.arg(value: T?, column: Column<T>): ReadOnlyProperty<Nothing?, Argument<T>> {
    // Create a new instance to ensure that the original nullable property remains unchanged when calling `TransactionManager.current().exec()`.
    val columnType = object : IColumnType<T> by column.columnType {
        override var nullable: Boolean = true
    }
    return bind(value, typeOf<T>(), columnType)
}

inline fun <reified T : Any> R2dbcTemplateBuilder.arg(value: T?, columnType: IColumnType<T>): ReadOnlyProperty<Nothing?, Argument<T>> {
    return bind(value, typeOf<T>(), columnType)
}

@PublishedApi
internal fun <T : Any> R2dbcTemplateBuilder.bind(value: T?, type: KType, columnType: IColumnType<T>): ReadOnlyProperty<Nothing?, Argument<T>> {
    return ReadOnlyProperty<Nothing?, Argument<T>> { _, property ->
        val name = property.name
        nameToValueMap[name] = Value(value, type, hint = columnType)
        Argument(name, value, type, columnType)
    }
}

internal class R2dbcTemplateImpl(private val sql: String, private val nameToValueMap: Map<String, Value<*>>) : R2dbcTemplate {
    override suspend fun <T> execute(transform: (Row) -> T?): Flow<T?>? {
        val dialect = R2dbcTemplateDialect()
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

class R2dbcTemplateDialect() : BuilderDialect, R2dbcDialect {
    override val driver: String = "komapper-exposed-r2dbc"

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

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        throw UnsupportedOperationException()
    }
}
