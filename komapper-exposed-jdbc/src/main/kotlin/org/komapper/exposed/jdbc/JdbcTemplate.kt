package org.komapper.exposed.jdbc

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.Value
import org.komapper.exposed.Argument
import org.komapper.exposed.SqlTemplateDialect
import org.komapper.exposed.resolveColumnType
import org.komapper.template.TwoWayTemplateStatementBuilderFactory
import java.sql.ResultSet
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface JdbcTemplate {
    fun <T> execute(transform: (ResultSet) -> T?): T?
}

class JdbcTemplateBuilder() {
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

fun jdbcTemplate(block: JdbcTemplateBuilder.() -> Unit): JdbcTemplate {
    val builder = JdbcTemplateBuilder()
    builder.block()
    val rawTemplate = builder.rawTemplate ?: error("The invocation of the build method is missing.")
    return JdbcTemplateImpl(rawTemplate, builder.nameToValueMap.toMap())
}

inline fun <reified T : Any> JdbcTemplateBuilder.arg(value: T?, column: Column<T>): ReadOnlyProperty<Nothing?, Argument<T>> {
    // Create a new instance to ensure that the original nullable property remains unchanged when calling `TransactionManager.current().exec()`.
    val columnType = object : IColumnType<T> by column.columnType {
        override var nullable: Boolean = true
    }
    return bind(value, typeOf<T>(), columnType)
}

inline fun <reified T : Any> JdbcTemplateBuilder.arg(value: T?, columnType: IColumnType<T>): ReadOnlyProperty<Nothing?, Argument<T>> {
    return bind(value, typeOf<T>(), columnType)
}

@PublishedApi
internal fun <T : Any> JdbcTemplateBuilder.bind(value: T?, type: KType, columnType: IColumnType<T>): ReadOnlyProperty<Nothing?, Argument<T>> {
    return ReadOnlyProperty<Nothing?, Argument<T>> { _, property ->
        val name = property.name
        nameToValueMap[name] = Value(value, type, hint = columnType)
        Argument(name, value, type, columnType)
    }
}

internal class JdbcTemplateImpl(private val sql: String, private val nameToValueMap: Map<String, Value<*>>) : JdbcTemplate {
    override fun <T> execute(transform: (ResultSet) -> T?): T? {
        val dialect = SqlTemplateDialect()
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
