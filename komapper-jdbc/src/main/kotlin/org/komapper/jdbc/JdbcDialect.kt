package org.komapper.jdbc

import org.komapper.core.Dialect
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.builder.DryRunSchemaStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.jdbc.spi.JdbcDialectFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ServiceLoader
import java.util.regex.Pattern
import kotlin.reflect.KClass

@ThreadSafe
interface JdbcDialect : Dialect {
    val dataTypes: List<DataType<*>>
    val subprotocol: String

    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any?
    fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any?
    fun getDataType(klass: KClass<*>): DataType<*>
    fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>)
    fun tryGetDataType(name: String): DataType<*>?
    fun isUniqueConstraintViolation(exception: SQLException): Boolean

    companion object {
        private val jdbcUrlPattern = Pattern.compile("^jdbc:([^:]*):.*")

        fun load(url: String, dataTypes: List<DataType<*>>): JdbcDialect {
            val subprotocol = extractJdbcSubprotocol(url)
            val loader = ServiceLoader.load(JdbcDialectFactory::class.java)
            val factory = loader.firstOrNull { it.supports(subprotocol) }
                ?: error(
                    "The dialect is not found for the JDBC url. " +
                        "Try to add the 'komapper-jdbc-$subprotocol' dependency. " +
                        "url=$url, subprotocol='$subprotocol'"
                )
            return factory.create(dataTypes)
        }

        private fun extractJdbcSubprotocol(url: String): String {
            val matcher = jdbcUrlPattern.matcher(url)
            if (matcher.matches()) {
                return matcher.group(1).lowercase()
            }
            error("The subprotocol in the JDBC URL is not found. url=$url")
        }
    }
}

abstract class AbstractJdbcDialect protected constructor(internalDataTypes: List<DataType<*>> = emptyList()) :
    JdbcDialect {

    @Suppress("MemberVisibilityCanBePrivate")
    protected val dataTypeMap: Map<KClass<*>, DataType<*>> = internalDataTypes.associateBy { it.klass }
    override val dataTypes = internalDataTypes
    override val openQuote: String = "\""
    override val closeQuote: String = "\""
    override val escapeSequence: String = "\\"

    override fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, index)
    }

    override fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, columnLabel)
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        val dataType = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as DataType<Any>
        dataType.setValue(ps, index, value)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        val dataType = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as DataType<Any>
        return dataType.toString(value)
    }

    override fun getDataTypeName(klass: KClass<*>): String {
        val dataType = getDataType(klass)
        return dataType.name
    }

    override fun getDataType(klass: KClass<*>): DataType<*> {
        return dataTypeMap[klass] ?: error(
            "The dataType is not found for the type \"${klass.qualifiedName}\"."
        )
    }

    override fun tryGetDataType(name: String): DataType<*>? {
        return dataTypeMap.values.firstOrNull {
            it.name.lowercase() == name.lowercase()
        }
    }

    protected fun getCause(exception: SQLException): SQLException =
        exception.filterIsInstance(SQLException::class.java).first()
}

internal object DryRunJdbcDialect : AbstractJdbcDialect() {

    override val subprotocol: String = "dry_run"

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getDataType(klass: KClass<*>): DataType<Any> {
        return AnyType("other")
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return DryRunSchemaStatementBuilder
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        throw UnsupportedOperationException()
    }
}
