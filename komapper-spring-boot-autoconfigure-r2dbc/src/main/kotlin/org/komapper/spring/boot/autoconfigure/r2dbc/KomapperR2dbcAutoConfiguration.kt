package org.komapper.spring.boot.autoconfigure.r2dbc

import io.r2dbc.spi.ConnectionFactory
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
import org.komapper.core.StatisticManager
import org.komapper.core.StatisticManagers
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.TemplateStatementBuilders
import org.komapper.r2dbc.DefaultR2dbcDataOperator
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDataTypeProviders
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcDialects
import org.komapper.r2dbc.R2dbcSession
import org.komapper.r2dbc.R2dbcUserDefinedDataTypeAdapter
import org.komapper.r2dbc.SimpleR2dbcDatabaseConfig
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import org.komapper.spring.r2dbc.SpringR2dbcTransactionSession
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.core.env.Environment
import org.springframework.transaction.ReactiveTransactionManager
import java.util.Optional
import java.util.UUID

@Suppress("unused")
@AutoConfiguration(after = [R2dbcAutoConfiguration::class, R2dbcTransactionManagerAutoConfiguration::class])
@ConditionalOnClass(R2dbcDatabase::class)
@ConditionalOnBean(ReactiveTransactionManager::class, ConnectionFactory::class)
open class KomapperR2dbcAutoConfiguration {
    companion object {
        private const val R2DBC_URL_PROPERTY = "spring.r2dbc.url"
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperDialect(environment: Environment): R2dbcDialect {
        val url = environment.getProperty(R2DBC_URL_PROPERTY)
            ?: error(
                "$R2DBC_URL_PROPERTY is not found. " +
                    "Specify it to the application.properties file or define the R2dbcDialect bean manually.",
            )
        val driver = R2dbcDialects.extractR2dbcDriver(url)
        return R2dbcDialects.get(driver)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperClockProvider(): ClockProvider {
        return DefaultClockProvider()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperExecutionOptions(): ExecutionOptions {
        return ExecutionOptions()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperLogger(): Logger {
        return Loggers.get()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperLoggerFacade(logger: Logger): LoggerFacade {
        return LoggerFacades.get(logger)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperSession(
        transactionManager: ReactiveTransactionManager,
        connectionFactory: ConnectionFactory,
    ): R2dbcSession {
        return SpringR2dbcTransactionSession(transactionManager, connectionFactory)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperStatementInspector(): StatementInspector {
        return StatementInspectors.get()
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional(ConditionalOnAnyR2dbcDataTypeBean::class)
    open fun komapperBeanDataTypeProvider(dataTypes: ObjectProvider<R2dbcDataType<*>>, userDefinedDataTypes: ObjectProvider<R2dbcUserDefinedDataType<*>>): R2dbcDataTypeProvider {
        val all = dataTypes + userDefinedDataTypes.map { R2dbcUserDefinedDataTypeAdapter(it) }
        val dupes = all.groupBy { it.type }.filterValues { it.size > 1 }
        if (dupes.isNotEmpty()) {
            throw IllegalStateException("Found multiple Beans for one or more DataTypes: ${dupes.keys}")
        }
        return R2dbcDataTypeProvider(*all.toTypedArray())
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperDataOperator(
        dialect: R2dbcDialect,
        dataTypeProvider: Optional<R2dbcDataTypeProvider>,
    ): R2dbcDataOperator {
        val provider = R2dbcDataTypeProviders.get(dialect.driver, dataTypeProvider.orElse(null))
        return DefaultR2dbcDataOperator(dialect, provider)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperTemplateStatementBuilder(
        dialect: R2dbcDialect,
        dataOperator: R2dbcDataOperator,
    ): TemplateStatementBuilder {
        return TemplateStatementBuilders.get(BuilderDialect(dialect, dataOperator))
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperStatistics(): StatisticManager {
        return StatisticManagers.get()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperDatabaseConfig(
        dialect: R2dbcDialect,
        clockProvider: ClockProvider,
        executionOptions: ExecutionOptions,
        logger: Logger,
        loggerFacade: LoggerFacade,
        session: R2dbcSession,
        statementInspector: StatementInspector,
        dataOperator: R2dbcDataOperator,
        connectionFactory: ConnectionFactory,
        templateStatementBuilder: TemplateStatementBuilder,
        statistics: StatisticManager,
    ): R2dbcDatabaseConfig {
        return SimpleR2dbcDatabaseConfig(
            id = UUID.randomUUID(),
            dialect = dialect,
            clockProvider = clockProvider,
            executionOptions = executionOptions,
            logger = logger,
            loggerFacade = loggerFacade,
            session = session,
            statementInspector = statementInspector,
            dataOperator = dataOperator,
            connectionFactory = connectionFactory,
            templateStatementBuilder = templateStatementBuilder,
            statisticManager = statistics,
        )
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperR2dbcDatabase(config: R2dbcDatabaseConfig): R2dbcDatabase {
        return R2dbcDatabase(config)
    }
}
