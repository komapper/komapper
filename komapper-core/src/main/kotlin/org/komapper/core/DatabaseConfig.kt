package org.komapper.core

import java.util.UUID

interface DatabaseConfig {
    val id: UUID
    val dialect: Dialect
    val clockProvider: ClockProvider
    val executionOptions: ExecutionOptions
    val logger: Logger
    val statementInspector: StatementInspector
    val templateStatementBuilder: TemplateStatementBuilder
}

object DryRunDatabaseConfig : DatabaseConfig {
    override val id: UUID
        get() = throw UnsupportedOperationException()
    override val dialect: Dialect = DryRunDialect
    override val logger: Logger
        get() = throw UnsupportedOperationException()
    override val clockProvider: ClockProvider = DefaultClockProvider()
    override val executionOptions: ExecutionOptions
        get() = throw UnsupportedOperationException()
    override val statementInspector: StatementInspector
        get() = throw UnsupportedOperationException()
    override val templateStatementBuilder: TemplateStatementBuilder
        get() = throw UnsupportedOperationException()
}
