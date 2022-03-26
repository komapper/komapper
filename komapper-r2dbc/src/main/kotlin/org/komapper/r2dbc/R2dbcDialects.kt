package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactoryOptions
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
        return getOrNull(driver) ?: error(
            "The dialect is not found. " +
                "Try to add the 'komapper-dialect-$driver-r2dbc' dependency. " +
                "driver='$driver'"
        )
    }

    private fun getOrNull(driver: String): R2dbcDialect? {
        val loader = ServiceLoader.load(R2dbcDialectFactory::class.java)
        val factory = loader.filter { it.supports(driver) }.findByPriority()
        return factory?.create()
    }

    fun getByOptions(options: ConnectionFactoryOptions): R2dbcDialect {
        val driverOption = options.getValue(ConnectionFactoryOptions.DRIVER)?.toString()
        val protocolOption = options.getValue(ConnectionFactoryOptions.PROTOCOL)?.toString()
        val dialect = sequenceOf(driverOption, protocolOption)
            .filterNotNull()
            .firstNotNullOfOrNull { getOrNull(it) }
        return dialect ?: error(
            "The dialect is not found. " +
                "driverOption='$driverOption', protocolOption='$protocolOption'"
        )
    }

    /**
     * @param url the R2DBC url
     */
    fun getByUrl(url: String): R2dbcDialect {
        val driver = extractR2dbcDriver(url)
        return get(driver)
    }

    /**
     * Extracts the R2DBC driver name from the R2DBC URL.
     *
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
