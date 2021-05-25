package org.komapper.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.core.Dialect
import org.komapper.r2dbc.spi.R2dbcDialectFactory
import java.util.ServiceLoader
import java.util.regex.Pattern
import kotlin.reflect.KClass

interface R2dbcDialect : Dialect {
    companion object {
        private val r2dbcUrlPattern = Pattern.compile("^r2dbc:([^:]*):.*")

        fun extractR2dbcDriver(url: String): String {
            val matcher = r2dbcUrlPattern.matcher(url)
            if (matcher.matches()) {
                return matcher.group(1).lowercase()
            }
            error("The driver is not found in the R2DBC URL. url=$url")
        }

        fun load(driver: String): R2dbcDialect {
            val loader = ServiceLoader.load(R2dbcDialectFactory::class.java)
            val factory = loader.firstOrNull { it.supports(driver) }
                ?: error(
                    "The dialect is not found. " +
                        "Try to add the 'komapper-r2dbc-dialect-$driver' dependency. " +
                        "driver='$driver'"
                )
            return factory.create()
        }
    }

    val driver: String

    fun setValue(statement: Statement, index: Int, value: Any?, valueClass: KClass<*>)
    fun getValue(row: Row, index: Int, valueClass: KClass<*>): Any?
    fun getValue(row: Row, columnLabel: String, valueClass: KClass<*>): Any?
}
