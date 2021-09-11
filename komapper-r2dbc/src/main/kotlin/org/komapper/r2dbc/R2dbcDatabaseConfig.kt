package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.ClockProvider
import org.komapper.core.DatabaseConfig
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.StdOutLogger
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.ThreadSafe
import org.komapper.core.spi.DefaultStatementInspector
import org.komapper.core.spi.StatementInspector
import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.r2dbc.spi.R2dbcSessionFactory
import java.util.ServiceLoader
import java.util.UUID

@ThreadSafe
interface R2dbcDatabaseConfig : DatabaseConfig {
    override val id: UUID
    override val dialect: R2dbcDialect
    override val clockProvider: ClockProvider
    override val executionOptions: ExecutionOptions
    override val logger: Logger

    val session: R2dbcSession
    override val statementInspector: StatementInspector

    // val dataFactory: DataFactory
    override val templateStatementBuilder: TemplateStatementBuilder
}

class DefaultR2dbcDatabaseConfig(
    connectionFactory: ConnectionFactory,
    override val dialect: R2dbcDialect
) : R2dbcDatabaseConfig {

    override val id: UUID = UUID.randomUUID()
    override val clockProvider: ClockProvider = DefaultClockProvider()
    override val executionOptions: ExecutionOptions = ExecutionOptions()
    override val logger: Logger = StdOutLogger()
    override val session: R2dbcSession by lazy {
        val loader = ServiceLoader.load(R2dbcSessionFactory::class.java)
        val factory = loader.firstOrNull()
        factory?.create(connectionFactory, logger) ?: DefaultR2DbcSession(connectionFactory)
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
