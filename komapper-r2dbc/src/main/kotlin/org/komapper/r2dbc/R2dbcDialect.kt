package org.komapper.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.core.Dialect
import org.komapper.core.StatementPart
import org.komapper.r2dbc.spi.R2dbcDialectFactory
import java.util.ServiceLoader
import java.util.regex.Pattern
import kotlin.reflect.KClass

interface R2dbcDialect : Dialect {

    companion object {
        private val r2dbcUrlPattern = Pattern.compile("^r2dbc[s]?:(tc:)?([^:]*):.*")

        fun load(driver: String): R2dbcDialect {
            val loader = ServiceLoader.load(R2dbcDialectFactory::class.java)
            val factory = loader.firstOrNull { it.supports(driver) }
                ?: error(
                    "The dialect is not found. " +
                        "Try to add the 'komapper-dialect-$driver-r2dbc' dependency. " +
                        "driver='$driver'"
                )
            return factory.create()
        }

        fun extractR2dbcDriver(url: String): String {
            val matcher = r2dbcUrlPattern.matcher(url)
            if (matcher.matches()) {
                return matcher.group(2).lowercase()
            }
            error("The driver is not found in the R2DBC URL. url=$url")
        }
    }

    val dataTypes: List<R2dbcDataType<*>>

    fun getValue(row: Row, index: Int, valueClass: KClass<*>): Any?
    fun getValue(row: Row, columnLabel: String, valueClass: KClass<*>): Any?
    fun setValue(statement: Statement, index: Int, value: Any?, valueClass: KClass<*>)
    fun getDataType(klass: KClass<*>): R2dbcDataType<*>
}

abstract class AbstractR2dbcDialect protected constructor(internalDataTypes: List<R2dbcDataType<*>> = emptyList()) :
    R2dbcDialect {

    @Suppress("MemberVisibilityCanBePrivate")
    protected val dataTypeMap: Map<KClass<*>, R2dbcDataType<*>> = internalDataTypes.associateBy { it.klass }
    override val dataTypes = internalDataTypes

    protected open fun getBinder(): Binder {
        return DefaultBinder
    }

    override fun replacePlaceHolder(index: Int, placeHolder: StatementPart.PlaceHolder): CharSequence {
        val bindMarker = getBinder()
        return bindMarker.replace(index, placeHolder)
    }

    override fun getValue(row: Row, index: Int, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(row, index)
    }

    override fun getValue(row: Row, columnLabel: String, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(row, columnLabel)
    }

    override fun setValue(statement: Statement, index: Int, value: Any?, valueClass: KClass<*>) {
        val dataType = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as R2dbcDataType<Any>
        val bindMarker = getBinder()
        return bindMarker.bind(statement, index, value, dataType)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        val dataType = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as R2dbcDataType<Any>
        return dataType.toString(value)
    }

    override fun getDataType(klass: KClass<*>): R2dbcDataType<*> {
        return dataTypeMap[klass] ?: error(
            "The dataType is not found for the type \"${klass.qualifiedName}\"."
        )
    }

    override fun getDataTypeName(klass: KClass<*>): String {
        val dataType = getDataType(klass)
        return dataType.name
    }
}
