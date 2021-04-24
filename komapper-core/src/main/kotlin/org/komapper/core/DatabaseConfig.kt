package org.komapper.core

import org.komapper.core.data.JdbcOption
import org.komapper.core.jdbc.DataType
import org.komapper.core.jdbc.SimpleDataSource
import org.komapper.core.spi.DatabaseSessionFactory
import org.komapper.core.spi.DialectFactory
import org.komapper.core.spi.TemplateStatementBuilderFactory
import java.util.ServiceLoader
import java.util.regex.Pattern
import javax.sql.DataSource

/**
 * A database configuration.
 *
 * @property name the name of this configuration. The name is used as a key to manage sequence values. The name must be unique.
 * @property dialect the dialect
 * @property logger the logger
 * @property clockProvider the clock provider
 * @property jdbcOption the jdbc configuration
 * @property session the session
 */
interface DatabaseConfig {
    val name: String
    val dialect: Dialect
    val logger: Logger
    val clockProvider: ClockProvider
    val jdbcOption: JdbcOption
    val session: DatabaseSession
    val templateStatementBuilder: TemplateStatementBuilder
}

open class DefaultDatabaseConfig(
    dataSource: DataSource,
    override val dialect: Dialect
) : DatabaseConfig {

    constructor(
        url: String,
        user: String = "",
        password: String = "",
        dataTypes: Set<DataType<*>> = emptySet()
    ) : this(SimpleDataSource(url, user, password), loadDialect(url, dataTypes))

    constructor(
        url: String,
        user: String = "",
        password: String = "",
        dialect: Dialect
    ) : this(SimpleDataSource(url, user, password), dialect)

    companion object {
        private val jdbcUrlPattern = Pattern.compile("^jdbc:([^:]*):.*")

        private fun loadDialect(url: String, dataTypes: Set<DataType<*>>): Dialect {
            val subprotocol = extractJdbcSubprotocol(url)
            val loader = ServiceLoader.load(DialectFactory::class.java)
            val factory = loader.firstOrNull { it.supports(subprotocol) }
                ?: error("The dialect is not found for the JDBC url. Add an appropriate dependency. url=$url, subprotocol='$subprotocol'")
            return factory.create(dataTypes)
        }

        private fun extractJdbcSubprotocol(url: String): String {
            val matcher = jdbcUrlPattern.matcher(url)
            if (matcher.matches()) {
                return matcher.group(1).toLowerCase()
            }
            error("The subprotocol in the JDBC URL is not found. url=$url")
        }
    }

    override val name: String = System.identityHashCode(object {}).toString()
    override val logger: Logger = StdOutLogger()
    override val clockProvider = DefaultClockProvider()
    override val jdbcOption: JdbcOption = JdbcOption(batchSize = 10)
    override val session: DatabaseSession by lazy {
        val loader = ServiceLoader.load(DatabaseSessionFactory::class.java)
        val factory = loader.firstOrNull()
        factory?.create(dataSource, logger) ?: DefaultDatabaseSession(dataSource)
    }

    override val templateStatementBuilder: TemplateStatementBuilder by lazy {
        val loader = ServiceLoader.load(TemplateStatementBuilderFactory::class.java)
        val factory = loader.firstOrNull()
            ?: error(
                "TemplateStatementBuilderFactory is not found. " +
                    "Add komapper-template dependency or override the templateStatementBuilder property."
            )
        factory.create(dialect)
    }
}

object DryRunDatabaseConfig : DatabaseConfig {
    override val name: String
        get() = throw UnsupportedOperationException()
    override val dialect: Dialect = DryRunDialect
    override val logger: Logger
        get() = throw UnsupportedOperationException()
    override val clockProvider: ClockProvider
        get() = throw UnsupportedOperationException()
    override val jdbcOption: JdbcOption
        get() = throw UnsupportedOperationException()
    override val session: DatabaseSession
        get() = throw UnsupportedOperationException()
    override val templateStatementBuilder: TemplateStatementBuilder
        get() = throw UnsupportedOperationException()
}
