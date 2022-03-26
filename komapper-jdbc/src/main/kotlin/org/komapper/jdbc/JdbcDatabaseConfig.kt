package org.komapper.jdbc

import org.komapper.core.AbstractDatabaseConfig
import org.komapper.core.ClockProvider
import org.komapper.core.DatabaseConfig
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.LoggerFacade
import org.komapper.core.StatementInspector
import org.komapper.core.TemplateStatementBuilder
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
     * The data operator.
     */
    override val dataOperator: JdbcDataOperator

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
    dialect: JdbcDialect,
    dataTypeProvider: JdbcDataTypeProvider? = null,
    clockProvider: ClockProvider = DefaultClockProvider(),
    executionOptions: ExecutionOptions = ExecutionOptions()
) : JdbcDatabaseConfig,
    AbstractDatabaseConfig<JdbcDialect>(dialect, clockProvider, executionOptions) {

    override val session: JdbcSession by lazy {
        JdbcSessions.get(dataSource, loggerFacade)
    }
    override val dataFactory: JdbcDataFactory by lazy {
        DefaultJdbcDataFactory(session)
    }
    override val dataOperator: JdbcDataOperator by lazy {
        val provider = JdbcDataTypeProviders.get(dialect.driver, dataTypeProvider)
        DefaultJdbcDataOperator(dialect, provider)
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
    override val dataFactory: JdbcDataFactory,
    override val dataOperator: JdbcDataOperator
) : JdbcDatabaseConfig
