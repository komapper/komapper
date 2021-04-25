package org.komapper.core

import org.komapper.core.data.JdbcOption
import org.komapper.core.jdbc.DataType
import org.komapper.core.jdbc.SimpleDataSource
import org.komapper.core.spi.DatabaseSessionFactory
import org.komapper.core.spi.LoggerFactory
import org.komapper.core.spi.TemplateStatementBuilderFactory
import java.util.ServiceLoader
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
interface DatabaseConfig : DatabaseConfigHolder {
    override val config: DatabaseConfig get() = this
    val name: String
    val dialect: Dialect
    val clockProvider: ClockProvider
    val jdbcOption: JdbcOption
    val logger: Logger
    val session: DatabaseSession
    val dataFactory: DataFactory
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
    ) : this(SimpleDataSource(url, user, password), Dialect.load(url, dataTypes))

    constructor(
        url: String,
        user: String = "",
        password: String = "",
        dialect: Dialect
    ) : this(SimpleDataSource(url, user, password), dialect)

    override val name: String = System.identityHashCode(object {}).toString()
    override val clockProvider: ClockProvider = DefaultClockProvider()
    override val jdbcOption: JdbcOption = JdbcOption(batchSize = 10)
    override val logger: Logger by lazy {
        val loader = ServiceLoader.load(LoggerFactory::class.java)
        val factory = loader.firstOrNull()
        factory?.create() ?: SqlStdOutLogger()
    }

    override val session: DatabaseSession by lazy {
        val loader = ServiceLoader.load(DatabaseSessionFactory::class.java)
        val factory = loader.firstOrNull()
        factory?.create(dataSource, logger) ?: DefaultDatabaseSession(dataSource)
    }

    override val dataFactory: DataFactory by lazy {
        DataFactoryImpl(session)
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
    override val dataFactory: DataFactory
        get() = throw UnsupportedOperationException()
    override val templateStatementBuilder: TemplateStatementBuilder
        get() = throw UnsupportedOperationException()
}
