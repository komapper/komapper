package org.komapper.r2dbc.spi

import org.komapper.core.ThreadSafe
import org.komapper.r2dbc.R2dbcDialect
import java.util.ServiceLoader
import java.util.regex.Pattern

@ThreadSafe
interface R2dbcDialectFactory {
    fun supports(driver: String): Boolean
    fun create(): R2dbcDialect
}

object R2dbcDialectProvider {
    private val r2dbcUrlPattern = Pattern.compile("^r2dbc[s]?:(tc:)?([^:]*):.*")

    fun get(driver: String): R2dbcDialect {
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
