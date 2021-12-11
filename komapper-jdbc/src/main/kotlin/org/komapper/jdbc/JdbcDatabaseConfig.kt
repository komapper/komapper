package org.komapper.jdbc

import org.komapper.core.ClockProvider
import org.komapper.core.DatabaseConfig
import org.komapper.core.DefaultClockProvider
import org.komapper.core.DefaultLoggerFacade
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.LoggerFacade
import org.komapper.core.Loggers
import org.komapper.core.StatementInspector
import org.komapper.core.StatementInspectors
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.TemplateStatementBuilders
import org.komapper.core.ThreadSafe
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
    val session: JdbcSession
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
    ) : this(SimpleDataSource(url, user, password), JdbcDialects.get(url, dataTypes))

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
        Loggers.get()
    }
    override val loggerFacade: LoggerFacade by lazy {
        DefaultLoggerFacade(logger)
    }
    override val session: JdbcSession by lazy {
        JdbcSessions.get(dataSource, loggerFacade)
    }
    override val statementInspector: StatementInspector by lazy {
        StatementInspectors.get()
    }
    override val dataFactory: JdbcDataFactory by lazy {
        DefaultJdbcDataFactory(session)
    }
    override val templateStatementBuilder: TemplateStatementBuilder by lazy {
        TemplateStatementBuilders.get(dialect)
    }
}
