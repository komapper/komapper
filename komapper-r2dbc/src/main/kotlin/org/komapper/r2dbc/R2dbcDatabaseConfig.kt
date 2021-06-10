package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.ClockProvider
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOption
import org.komapper.core.Logger
import org.komapper.core.StdOutLogger
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.ThreadSafe
import org.komapper.core.spi.DefaultStatementInspector
import org.komapper.core.spi.StatementInspector
import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.r2dbc.spi.R2dbcDatabaseSessionFactory
import java.util.ServiceLoader
import java.util.UUID

@ThreadSafe
interface R2dbcDatabaseConfig {
    val id: UUID
    val dialect: R2dbcDialect
    val clockProvider: ClockProvider
    val executionOption: ExecutionOption
    val logger: Logger

    val session: R2dbcDatabaseSession
    val statementInspector: StatementInspector

    // val dataFactory: DataFactory
    val templateStatementBuilder: TemplateStatementBuilder
}

class DefaultR2dbcDatabaseConfig(
    connectionFactory: ConnectionFactory,
    override val dialect: R2dbcDialect
) : R2dbcDatabaseConfig {

    override val id: UUID = UUID.randomUUID()
    override val clockProvider: ClockProvider = DefaultClockProvider()
    override val executionOption: ExecutionOption = ExecutionOption()
    override val logger: Logger = StdOutLogger()
    override val session: R2dbcDatabaseSession by lazy {
        val loader = ServiceLoader.load(R2dbcDatabaseSessionFactory::class.java)
        val factory = loader.firstOrNull()
        factory?.create(connectionFactory, logger) ?: DefaultR2dbcDatabaseSession(connectionFactory)
    }
    override val statementInspector: StatementInspector by lazy {
        val loader = ServiceLoader.load(StatementInspector::class.java)
        loader.firstOrNull() ?: DefaultStatementInspector()
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

object DryRunR2dbcDatabaseConfig : R2dbcDatabaseConfig {
    override val id: UUID
        get() = throw UnsupportedOperationException()
    override val dialect: R2dbcDialect = DryRunR2dbcDialect
    override val logger: Logger
        get() = throw UnsupportedOperationException()
    override val clockProvider: ClockProvider
        get() = throw UnsupportedOperationException()
    override val executionOption: ExecutionOption
        get() = throw UnsupportedOperationException()
    override val session: R2dbcDatabaseSession
        get() = throw UnsupportedOperationException()
    override val statementInspector: StatementInspector
        get() = throw UnsupportedOperationException()
    override val templateStatementBuilder: TemplateStatementBuilder
        get() = throw UnsupportedOperationException()
}
