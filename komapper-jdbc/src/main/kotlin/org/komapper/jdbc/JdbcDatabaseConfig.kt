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
 * Represents a database configuration for JDBC access.
 */
@ThreadSafe
interface JdbcDatabaseConfig : DatabaseConfig {
    /**
     * The dialect.
     */
    override val dialect: JdbcDialect

    /**
     * The session to the database.
     */
    val session: JdbcSession

    /**
     * The data factory.
     */
    val dataFactory: JdbcDataFactory
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
    ) : this(SimpleDataSource(url, user, password), JdbcDialects.getByUrl(url, dataTypes))

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

class SimpleJdbcDatabaseConfig(
    override val id: UUID,
    override val clockProvider: ClockProvider,
    override val executionOptions: ExecutionOptions,
    override val logger: Logger,
    override val loggerFacade: LoggerFacade,
    override val statementInspector: StatementInspector,
    override val templateStatementBuilder: TemplateStatementBuilder,
    override val dialect: JdbcDialect,
    override val session: JdbcSession,
    override val dataFactory: JdbcDataFactory
) : JdbcDatabaseConfig
