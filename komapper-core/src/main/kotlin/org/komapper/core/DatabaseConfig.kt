package org.komapper.core

import java.util.UUID

/**
 * Database configuration.
 */
@ThreadSafe
interface DatabaseConfig {
    /**
     * The identity of this configuration.
     */
    val id: UUID
    val dialect: Dialect
    val clockProvider: ClockProvider
    val executionOptions: ExecutionOptions
    val logger: Logger
    val loggerFacade: LoggerFacade
    val statementInspector: StatementInspector
    val templateStatementBuilder: TemplateStatementBuilder
}

abstract class AbstractDatabaseConfig<DIALECT : Dialect>(
    override val dialect: DIALECT,
    override val clockProvider: ClockProvider = DefaultClockProvider(),
    override val executionOptions: ExecutionOptions = ExecutionOptions()
) : DatabaseConfig {
    override val id: UUID = UUID.randomUUID()
    override val logger: Logger by lazy {
        Loggers.get()
    }
    override val loggerFacade: LoggerFacade by lazy {
        DefaultLoggerFacade(logger)
    }
    override val statementInspector: StatementInspector by lazy {
        StatementInspectors.get()
    }
    override val templateStatementBuilder: TemplateStatementBuilder by lazy {
        TemplateStatementBuilders.get(dialect)
    }
}

/**
 * Database configuration for a dry run.
 */
object DryRunDatabaseConfig : DatabaseConfig {
    override val id: UUID
        get() = throw UnsupportedOperationException()
    override val dialect: Dialect = DryRunDialect
    override val logger: Logger
        get() = throw UnsupportedOperationException()
    override val loggerFacade: LoggerFacade
        get() = throw UnsupportedOperationException()
    override val clockProvider: ClockProvider = DefaultClockProvider()
    override val executionOptions: ExecutionOptions
        get() = throw UnsupportedOperationException()
    override val statementInspector: StatementInspector
        get() = throw UnsupportedOperationException()
    override val templateStatementBuilder: TemplateStatementBuilder
        get() = throw UnsupportedOperationException()
}
