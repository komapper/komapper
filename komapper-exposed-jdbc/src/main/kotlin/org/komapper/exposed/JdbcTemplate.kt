package org.komapper.exposed

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.v1.core.BasicBinaryColumnType
import org.jetbrains.exposed.v1.core.BlobColumnType
import org.jetbrains.exposed.v1.core.BooleanColumnType
import org.jetbrains.exposed.v1.core.ByteColumnType
import org.jetbrains.exposed.v1.core.CharacterColumnType
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.DoubleColumnType
import org.jetbrains.exposed.v1.core.FloatColumnType
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.IntegerColumnType
import org.jetbrains.exposed.v1.core.LongColumnType
import org.jetbrains.exposed.v1.core.ShortColumnType
import org.jetbrains.exposed.v1.core.UByteColumnType
import org.jetbrains.exposed.v1.core.UIntegerColumnType
import org.jetbrains.exposed.v1.core.ULongColumnType
import org.jetbrains.exposed.v1.core.UShortColumnType
import org.jetbrains.exposed.v1.core.UUIDColumnType
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.komapper.core.BuilderDialect
import org.komapper.core.DataType
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.Value
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.template.TwoWayTemplateStatementBuilderFactory
import java.sql.ResultSet
import java.util.UUID
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface JdbcTemplate {
    fun <T> exec(transform: (ResultSet) -> T?): T?
}

fun JdbcTemplate(
    @Language("sql") sql: String,
    nameToValueMap: Map<String, Value<*>> = emptyMap(),
): JdbcTemplate {
    val context = JdbcTemplateContext(sql, nameToValueMap)
    return JdbcTemplateImpl(context)
}

class JdbcTemplateBuilder() {
    fun build(
        @Language("sql") template: String,
    ) {
        this.template = template
    }

    @PublishedApi
    internal val nameToValueMap: MutableMap<String, Value<*>> = mutableMapOf()

    @PublishedApi
    internal var template: String? = null

    @PublishedApi
    internal var counter = 0
}

fun buildJdbcTemplate(block: JdbcTemplateBuilder.() -> Unit): JdbcTemplate {
    val builder = JdbcTemplateBuilder()
    builder.block()
    val sql = builder.template ?: error("The invocation of the build method is missing.")
    return JdbcTemplate(sql, builder.nameToValueMap.toMap())
}

inline fun <reified T : Any> JdbcTemplateBuilder.bind(value: T?, column: Column<T>): BoundValue<T> {
    val name = column.name
    val type = typeOf<T>()
    // Create a new instance to ensure that the original nullable property remains unchanged when calling `TransactionManager.current().exec()`.
    val columnType = object : IColumnType<T> by column.columnType {
        override var nullable: Boolean = true
    }
    nameToValueMap[name] = Value(value, type, hint = columnType)
    return BoundValue(name, value, type, columnType)
}

inline fun <reified T : Any> JdbcTemplateBuilder.bind(value: T?, columnType: ColumnType<T>, name: String? = null): BoundValue<T> {
    val name = name ?: "_komapper_anonymous_value_${counter++}"
    val type = typeOf<T>()
    nameToValueMap[name] = Value(value, type, hint = columnType)
    return BoundValue(name, value, type, columnType)
}

data class BoundValue<T>(
    val name: String,
    val value: T?,
    val type: KType,
    val columnType: IColumnType<T>,
) : CharSequence {
    override val length: Int get() = name.length
    override fun get(index: Int): Char = name[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = name.subSequence(startIndex, endIndex)
    override fun toString(): String = name
}

internal data class JdbcTemplateContext(
    val sql: String,
    val nameToValueMap: Map<String, Value<*>>,
)

internal class JdbcTemplateImpl(private val context: JdbcTemplateContext) : JdbcTemplate {
    override fun <T> exec(transform: (ResultSet) -> T?): T? {
        val dialect = JdbcBuilderDialect()
        val builder = TwoWayTemplateStatementBuilderFactory().create(dialect)
        val extensions = TemplateBuiltinExtensions { dialect.escape(it) }
        val statement = builder.build(context.sql, context.nameToValueMap, extensions)
        val sql = statement.toSql(dialect::createBindVariable)
        val args = statement.args.map {
            val columnType = it.hint as? IColumnType<*> ?: resolveColumnType(it.type)
            columnType to it.any
        }
        return TransactionManager.current().exec(stmt = sql, args = args, transform = transform)
    }

    // TODO
    private fun resolveColumnType(type: KType): IColumnType<*> {
        return when (type) {
            typeOf<String>() -> VarCharColumnType()
            typeOf<Byte>() -> ByteColumnType()
            typeOf<UByte>() -> UByteColumnType()
            typeOf<Short>() -> ShortColumnType()
            typeOf<UShort>() -> UShortColumnType()
            typeOf<Int>() -> IntegerColumnType()
            typeOf<UInt>() -> UIntegerColumnType()
            typeOf<Long>() -> LongColumnType()
            typeOf<ULong>() -> ULongColumnType()
            typeOf<Boolean>() -> BooleanColumnType()
            typeOf<ByteArray>() -> BasicBinaryColumnType()
            typeOf<ExposedBlob>() -> BlobColumnType()
            typeOf<Char>() -> CharacterColumnType()
            typeOf<Float>() -> FloatColumnType()
            typeOf<Double>() -> DoubleColumnType()
            typeOf<UUID>() -> UUIDColumnType()

            else -> throw UnsupportedOperationException("Unsupported type: $type")
        }
    }
}

internal class JdbcBuilderDialect() : BuilderDialect {
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
