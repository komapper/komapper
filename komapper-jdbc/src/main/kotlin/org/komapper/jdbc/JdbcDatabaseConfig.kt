package org.komapper.jdbc

import org.komapper.core.ClockProvider
import org.komapper.core.DatabaseConfig
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.StdOutLogger
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.ThreadSafe
import org.komapper.core.spi.DefaultStatementInspector
import org.komapper.core.spi.LoggerFactory
import org.komapper.core.spi.StatementInspector
import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.jdbc.spi.JdbcDatabaseSessionFactory
import java.util.ServiceLoader
import java.util.UUID
import javax.sql.DataSource

/**
 * A database configuration.
 *
 * @property id the id of this configuration. The id is used as a key to manage sequence values. The name must be unique.
 * @property dialect the dialect
 * @property logger the logger
 * @property clockProvider the clock provider
 * @property executionOptions the jdbc configuration
 * @property session the session
 */
@ThreadSafe
interface JdbcDatabaseConfig : DatabaseConfig {
    override val id: UUID
    override val dialect: JdbcDialect
    override val clockProvider: ClockProvider
    override val executionOptions: ExecutionOptions
    override val logger: Logger
    val session: JdbcDatabaseSession
    override val statementInspector: StatementInspector
    val dataFactory: JdbcDataFactory
    override val templateStatementBuilder: TemplateStatementBuilder
}

open class DefaultJdbcDatabaseConfig(
    dataSource: DataSource,
    override val dialect: JdbcDialect
) : JdbcDatabaseConfig {

    constructor(
        url: String,
        user: String = "",
        password: String = "",
        dataTypes: List<JdbcDataType<*>> = emptyList()
    ) : this(SimpleDataSource(url, user, password), JdbcDialect.load(url, dataTypes))

    constructor(
        url: String,
        user: String = "",
        password: String = "",
        dialect: JdbcDialect
    ) : this(SimpleDataSource(url, user, password), dialect)

    override val id: UUID = UUID.randomUUID()
    override val clockProvider: ClockProvider = DefaultClockProvider()
    override val executionOptions: ExecutionOptions = ExecutionOptions(batchSize = 10)
    override val logger: Logger by lazy {
        val loader = ServiceLoader.load(LoggerFactory::class.java)
        val factory = loader.firstOrNull()
        factory?.create() ?: StdOutLogger()
    }

    override val session: JdbcDatabaseSession by lazy {
        val loader = ServiceLoader.load(JdbcDatabaseSessionFactory::class.java)
        val factory = loader.firstOrNull()
        factory?.create(dataSource, logger) ?: DefaultJdbcDatabaseSession(dataSource)
    }

    override val statementInspector: StatementInspector by lazy {
        val loader = ServiceLoader.load(StatementInspector::class.java)
        loader.firstOrNull() ?: DefaultStatementInspector()
    }

    override val dataFactory: JdbcDataFactory by lazy {
        DefaultJdbcDataFactory(session)
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
