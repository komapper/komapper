package org.komapper.r2dbc

import org.komapper.core.spi.findByPriority
import org.komapper.r2dbc.spi.R2dbcDialectFactory
import java.util.ServiceLoader
import java.util.regex.Pattern

/**
 * The provider of [R2dbcDialect]
 */
object R2dbcDialects {
    private val r2dbcUrlPattern = Pattern.compile("^r2dbc[s]?:(tc:)?([^:]*):.*")

    /**
     * @param driver the R2DBC driver name
     * @return the [R2dbcDialect]
     */
    fun get(driver: String): R2dbcDialect {
        val loader = ServiceLoader.load(R2dbcDialectFactory::class.java)
        val factory = loader.filter { it.supports(driver) }.findByPriority()
            ?: error(
                "The dialect is not found. " +
                    "Try to add the 'komapper-dialect-$driver-r2dbc' dependency. " +
                    "driver='$driver'"
            )
        return factory.create()
    }

    /**
     * Extracts the R2DBC driver name from the R2DBC URL.
     * @param url the R2DBC URL
     * @return the R2DBC driver name
     */
    fun extractR2dbcDriver(url: String): String {
        val matcher = r2dbcUrlPattern.matcher(url)
        if (matcher.matches()) {
            return matcher.group(2).lowercase()
        }
        error("The driver is not found in the R2DBC URL. url=$url")
    }
}
