package org.komapper.jdbc.spi

import org.komapper.core.ThreadSafe
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDialect
import java.util.ServiceLoader
import java.util.regex.Pattern

@ThreadSafe
interface JdbcDialectFactory {
    fun supports(driver: String): Boolean
    fun create(dataTypes: List<JdbcDataType<*>>): JdbcDialect
}

object JdbcDialectProvider {
    private val jdbcUrlPattern = Pattern.compile("^jdbc:(tc:)?([^:]*):.*")

    fun get(url: String, dataTypes: List<JdbcDataType<*>>): JdbcDialect {
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

    internal fun extractJdbcDriver(url: String): String {
        val matcher = jdbcUrlPattern.matcher(url)
        if (matcher.matches()) {
            return matcher.group(2).lowercase()
        }
        error("The driver in the JDBC URL is not found. url=$url")
    }
}
