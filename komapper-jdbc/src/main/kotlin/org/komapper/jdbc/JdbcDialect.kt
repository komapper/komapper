package org.komapper.jdbc

import org.komapper.core.Dialect
import org.komapper.jdbc.spi.JdbcDialectFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ServiceLoader
import java.util.regex.Pattern
import kotlin.reflect.KClass

interface JdbcDialect : Dialect {

    companion object {
        private val jdbcUrlPattern = Pattern.compile("^jdbc:(tc:)?([^:]*):.*")

        fun load(url: String, dataTypes: List<JdbcDataType<*>>): JdbcDialect {
            val driver = extractJdbcDriver(url)
            val loader = ServiceLoader.load(JdbcDialectFactory::class.java)
            val factory = loader.firstOrNull { it.supports(driver) }
                ?: error(
                    "The dialect is not found for the JDBC url. " +
                        "Try to add the 'komapper-dialect-$driver-jdbc' dependency. " +
                        "url=$url, driver='$driver'"
                )
            return factory.create(dataTypes)
        }

        private fun extractJdbcDriver(url: String): String {
            val matcher = jdbcUrlPattern.matcher(url)
            if (matcher.matches()) {
                return matcher.group(2).lowercase()
            }
            error("The driver in the JDBC URL is not found. url=$url")
        }
    }

    val dataTypes: List<JdbcDataType<*>>

    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any?
    fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any?
    fun getDataType(klass: KClass<*>): JdbcDataType<*>
    fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>)
    fun tryGetDataType(name: String): JdbcDataType<*>?
    fun isUniqueConstraintViolation(exception: SQLException): Boolean
}

abstract class AbstractJdbcDialect protected constructor(internalDataTypes: List<JdbcDataType<*>> = emptyList()) :
    JdbcDialect {

    @Suppress("MemberVisibilityCanBePrivate")
    protected val dataTypeMap: Map<KClass<*>, JdbcDataType<*>> = internalDataTypes.associateBy { it.klass }
    override val dataTypes = internalDataTypes

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
        dataType as JdbcDataType<Any>
        dataType.setValue(ps, index, value)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        val dataType = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as JdbcDataType<Any>
        return dataType.toString(value)
    }

    override fun getDataTypeName(klass: KClass<*>): String {
        val dataType = getDataType(klass)
        return dataType.name
    }

    override fun getDataType(klass: KClass<*>): JdbcDataType<*> {
        return dataTypeMap[klass] ?: error(
            "The dataType is not found for the type \"${klass.qualifiedName}\"."
        )
    }

    override fun tryGetDataType(name: String): JdbcDataType<*>? {
        return dataTypeMap.values.firstOrNull {
            it.name.lowercase() == name.lowercase()
        }
    }

    protected fun getCause(exception: SQLException): SQLException =
        exception.filterIsInstance(SQLException::class.java).first()
}
