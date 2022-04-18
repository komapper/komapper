package org.komapper.micronaut.autoconfigure.jdbc

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.jdbc.BasicJdbcConfiguration
import io.micronaut.transaction.TransactionOperations
import jakarta.inject.Singleton
import org.komapper.core.BuilderDialect
import org.komapper.core.ClockProvider
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.LoggerFacade
import org.komapper.core.LoggerFacades
import org.komapper.core.Loggers
import org.komapper.core.StatementInspector
import org.komapper.core.StatementInspectors
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.TemplateStatementBuilders
import org.komapper.jdbc.DefaultJdbcDataFactory
import org.komapper.jdbc.DefaultJdbcDataOperator
import org.komapper.jdbc.JdbcDataFactory
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDataTypeProviders
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcDialects
import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.SimpleJdbcDatabaseConfig
import org.komapper.micronaut.jdbc.MicronautTransactionSession
import java.sql.Connection
import java.util.Optional
import java.util.UUID
import javax.sql.DataSource

@Factory
open class KomapperJdbcAutoConfiguration {

    @Singleton
    @Requires(missingBeans = [JdbcDialect::class])
    open fun dialect(configuration: BasicJdbcConfiguration): JdbcDialect {
        val url = configuration.configuredUrl
        return JdbcDialects.getByUrl(url)
    }

    @Singleton
    @Requires(missingBeans = [ClockProvider::class])
    open fun clockProvider(): ClockProvider {
        return DefaultClockProvider()
    }

    @Singleton
    @Requires(missingBeans = [ExecutionOptions::class])
    open fun executionOptions(): ExecutionOptions {
        return ExecutionOptions()
    }

    @Singleton
    @Requires(missingBeans = [Logger::class])
    open fun logger(): Logger {
        return Loggers.get()
    }

    @Singleton
    @Requires(missingBeans = [LoggerFacade::class])
    open fun loggerFacade(logger: Logger): LoggerFacade {
        return LoggerFacades.get(logger)
    }

    @Singleton
    @Requires(missingBeans = [JdbcSession::class])
    open fun databaseSession(
        transactionOperations: TransactionOperations<Connection>,
        dataSource: DataSource
    ): JdbcSession {
        return MicronautTransactionSession(transactionOperations, dataSource)
    }

    @Singleton
    @Requires(missingBeans = [StatementInspector::class])
    open fun statementInspector(): StatementInspector {
        return StatementInspectors.get()
    }

    @Singleton
    @Requires(missingBeans = [JdbcDataFactory::class])
    open fun dataFactory(databaseSession: JdbcSession): JdbcDataFactory {
        return DefaultJdbcDataFactory(databaseSession)
    }

    @Singleton
    @Requires(missingBeans = [JdbcDataOperator::class])
    open fun dataOperator(dialect: JdbcDialect, dataTypeProvider: Optional<JdbcDataTypeProvider>): JdbcDataOperator {
        val provider = JdbcDataTypeProviders.get(dialect.driver, dataTypeProvider.orElse(null))
        return DefaultJdbcDataOperator(dialect, provider)
    }

    @Singleton
    @Requires(missingBeans = [TemplateStatementBuilder::class])
    open fun templateStatementBuilder(dialect: JdbcDialect, dataOperator: JdbcDataOperator): TemplateStatementBuilder {
        return TemplateStatementBuilders.get(BuilderDialect(dialect, dataOperator))
    }

    @Singleton
    @Requires(missingBeans = [JdbcDatabaseConfig::class])
    open fun databaseConfig(
        dialect: JdbcDialect,
        clockProvider: ClockProvider,
        executionOptions: ExecutionOptions,
        logger: Logger,
        loggerFacade: LoggerFacade,
        session: JdbcSession,
        statementInspector: StatementInspector,
        dataFactory: JdbcDataFactory,
        dataOperator: JdbcDataOperator,
        templateStatementBuilder: TemplateStatementBuilder
    ): JdbcDatabaseConfig {
        return SimpleJdbcDatabaseConfig(
            id = UUID.randomUUID(),
            dialect = dialect,
            clockProvider = clockProvider,
            executionOptions = executionOptions,
            logger = logger,
            loggerFacade = loggerFacade,
            session = session,
            statementInspector = statementInspector,
            dataFactory = dataFactory,
            dataOperator = dataOperator,
            templateStatementBuilder = templateStatementBuilder,
        )
    }

    @Singleton
    @Requires(missingBeans = [JdbcDatabase::class])
    open fun database(config: JdbcDatabaseConfig): JdbcDatabase {
        return JdbcDatabase(config)
    }
}
