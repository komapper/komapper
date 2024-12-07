package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.AbstractDatabaseConfig
import org.komapper.core.ClockProvider
import org.komapper.core.DatabaseConfig
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.LoggerFacade
import org.komapper.core.StatementInspector
import org.komapper.core.Statistics
import org.komapper.core.TemplateStatementBuilder
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
     * The data operator.
     */
    override val dataOperator: R2dbcDataOperator

    /**
     * The session to the database.
     */
    val session: R2dbcSession

    /**
     * The connection factory.
     */
    val connectionFactory: ConnectionFactory
}

open class DefaultR2dbcDatabaseConfig(
    override val connectionFactory: ConnectionFactory,
    dialect: R2dbcDialect,
    dataTypeProvider: R2dbcDataTypeProvider? = null,
    clockProvider: ClockProvider = DefaultClockProvider(),
    executionOptions: ExecutionOptions = ExecutionOptions(),
    enableStatistics: Boolean = false,
) : R2dbcDatabaseConfig,
    AbstractDatabaseConfig<R2dbcDialect>(dialect, clockProvider, executionOptions, enableStatistics) {
    override val session: R2dbcSession by lazy {
        R2dbcSessions.get(connectionFactory, loggerFacade)
    }

    override val dataOperator: R2dbcDataOperator by lazy {
        val provider = R2dbcDataTypeProviders.get(dialect.driver, dataTypeProvider)
        DefaultR2dbcDataOperator(dialect, provider)
    }
}

class SimpleR2dbcDatabaseConfig(
    override val id: UUID,
    override val clockProvider: ClockProvider,
    override val executionOptions: ExecutionOptions,
    override val logger: Logger,
    override val loggerFacade: LoggerFacade,
    override val statementInspector: StatementInspector,
    override val templateStatementBuilder: TemplateStatementBuilder,
    override val dialect: R2dbcDialect,
    override val session: R2dbcSession,
    override val dataOperator: R2dbcDataOperator,
    override val connectionFactory: ConnectionFactory,
    override val statistics: Statistics,
) : R2dbcDatabaseConfig
