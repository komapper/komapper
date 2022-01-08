package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactory
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

/**
 * Represents a database configuration for R2DBC access.
 */
@ThreadSafe
interface R2dbcDatabaseConfig : DatabaseConfig {

    /**
     * The dialect.
     */
    override val dialect: R2dbcDialect

    /**
     * The session to the database.
     */
    val session: R2dbcSession
}

class DefaultR2dbcDatabaseConfig(
    connectionFactory: ConnectionFactory,
    override val dialect: R2dbcDialect
) : R2dbcDatabaseConfig {

    override val id: UUID = UUID.randomUUID()
    override val clockProvider: ClockProvider = DefaultClockProvider()
    override val executionOptions: ExecutionOptions = ExecutionOptions()
    override val logger: Logger by lazy {
        Loggers.get()
    }
    override val loggerFacade: LoggerFacade by lazy {
        DefaultLoggerFacade(logger)
    }
    override val session: R2dbcSession by lazy {
        R2dbcSessions.get(connectionFactory, loggerFacade)
    }
    override val statementInspector: StatementInspector by lazy {
        StatementInspectors.get()
    }
    override val templateStatementBuilder: TemplateStatementBuilder by lazy {
        TemplateStatementBuilders.get(dialect)
    }
}
