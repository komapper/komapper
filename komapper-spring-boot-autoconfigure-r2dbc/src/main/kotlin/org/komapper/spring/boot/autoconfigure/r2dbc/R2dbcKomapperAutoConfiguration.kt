package org.komapper.spring.boot.autoconfigure.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.ClockProvider
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
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcDialects
import org.komapper.r2dbc.R2dbcSession
import org.komapper.r2dbc.SimpleR2dbcDatabaseConfig
import org.komapper.spring.r2dbc.ReactiveTransactionSession
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.transaction.ReactiveTransactionManager
import java.util.UUID

@Suppress("unused")
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(R2dbcDatabase::class)
@ImportAutoConfiguration(value = [R2dbcAutoConfiguration::class, R2dbcTransactionManagerAutoConfiguration::class])
open class R2dbcKomapperAutoConfiguration {

    companion object {
        private const val R2DBC_URL_PROPERTY = "spring.r2dbc.url"
    }

    @Bean
    @ConditionalOnMissingBean
    open fun r2dbcDialect(environment: Environment): R2dbcDialect {
        val url = environment.getProperty(R2DBC_URL_PROPERTY)
            ?: error(
                "$R2DBC_URL_PROPERTY is not found. " +
                    "Specify it to the application.properties file or define the R2dbcDialect bean manually."
            )
        val driver = R2dbcDialects.extractR2dbcDriver(url)
        return R2dbcDialects.get(driver)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun clockProvider(): ClockProvider {
        return DefaultClockProvider()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun executionOptions(): ExecutionOptions {
        return ExecutionOptions()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun logger(): Logger {
        return Loggers.get()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun loggerFacade(logger: Logger): LoggerFacade {
        return DefaultLoggerFacade(logger)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun r2dbcDatabaseSession(
        transactionManager: ReactiveTransactionManager,
        connectionFactory: ConnectionFactory
    ): R2dbcSession {
        return ReactiveTransactionSession(transactionManager, connectionFactory)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun statementInspector(): StatementInspector {
        return StatementInspectors.get()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun templateStatementBuilder(dialect: R2dbcDialect): TemplateStatementBuilder {
        return TemplateStatementBuilders.get(dialect)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun databaseConfig(
        r2dbcDialect: R2dbcDialect,
        clockProvider: ClockProvider,
        executionOptions: ExecutionOptions,
        logger: Logger,
        loggerFacade: LoggerFacade,
        session: R2dbcSession,
        statementInspector: StatementInspector,
        templateStatementBuilder: TemplateStatementBuilder
    ): R2dbcDatabaseConfig {
        return SimpleR2dbcDatabaseConfig(
            id = UUID.randomUUID(),
            dialect = r2dbcDialect,
            clockProvider = clockProvider,
            executionOptions = executionOptions,
            logger = logger,
            loggerFacade = loggerFacade,
            session = session,
            statementInspector = statementInspector,
            templateStatementBuilder = templateStatementBuilder,
        )
    }

    @Bean
    @ConditionalOnMissingBean
    open fun r2dbcDatabase(config: R2dbcDatabaseConfig): R2dbcDatabase {
        return R2dbcDatabase(config)
    }
}
