package org.komapper.spring.boot.autoconfigure.jdbc

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
import org.komapper.jdbc.DefaultJdbcDataFactory
import org.komapper.jdbc.DefaultJdbcDataOperator
import org.komapper.jdbc.JdbcDataFactory
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDataTypeProviders
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcDialects
import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.JdbcUserDefinedDataTypeAdapter
import org.komapper.jdbc.SimpleJdbcDatabaseConfig
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import org.komapper.spring.jdbc.SpringJdbcTransactionSession
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.core.env.Environment
import org.springframework.transaction.PlatformTransactionManager
import java.util.Optional
import java.util.UUID
import javax.sql.DataSource

@Suppress("unused")
@AutoConfiguration(after = [DataSourceAutoConfiguration::class, DataSourceTransactionManagerAutoConfiguration::class])
@ConditionalOnClass(JdbcDatabase::class)
@ConditionalOnBean(PlatformTransactionManager::class, DataSource::class)
open class KomapperJdbcAutoConfiguration {
    companion object {
        private const val DATASOURCE_URL_PROPERTY = "spring.datasource.url"
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperDialect(environment: Environment, connectionDetailsProvider: ObjectProvider<JdbcConnectionDetails>): JdbcDialect {
        val connectionDetails = connectionDetailsProvider.getIfAvailable()
        val url = if (connectionDetails != null) {
            connectionDetails.jdbcUrl
        } else {
            environment.getProperty(DATASOURCE_URL_PROPERTY)
        }
        checkNotNull(url) {
            "Komapper JdbcDialect was not resolved. To fix this, do one of the following: " +
                "define a JdbcConnectionDetails bean, " +
                "set the $DATASOURCE_URL_PROPERTY property, " +
                "or define a JdbcDialect bean manually."
        }
        return JdbcDialects.getByUrl(url)
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
    open fun komapperSession(transactionManager: PlatformTransactionManager, dataSource: DataSource): JdbcSession {
        return SpringJdbcTransactionSession(transactionManager, dataSource)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperStatementInspector(): StatementInspector {
        return StatementInspectors.get()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperDataFactory(databaseSession: JdbcSession): JdbcDataFactory {
        return DefaultJdbcDataFactory(databaseSession)
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional(ConditionalOnAnyJdbcDataTypeBean::class)
    open fun komapperBeanDataTypeProvider(dataTypes: ObjectProvider<JdbcDataType<*>>, userDefinedDataTypes: ObjectProvider<JdbcUserDefinedDataType<*>>): JdbcDataTypeProvider {
        val all = dataTypes + userDefinedDataTypes.map { JdbcUserDefinedDataTypeAdapter(it) }
        val dupes = all.groupBy { it.type }.filterValues { it.size > 1 }
        if (dupes.isNotEmpty()) {
            throw IllegalStateException("Found multiple Beans for one or more DataTypes: ${dupes.keys}")
        }
        return JdbcDataTypeProvider(*all.toTypedArray())
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperDataOperator(
        dialect: JdbcDialect,
        dataTypeProvider: Optional<JdbcDataTypeProvider>,
    ): JdbcDataOperator {
        val provider = JdbcDataTypeProviders.get(dialect.driver, dataTypeProvider.orElse(null))
        return DefaultJdbcDataOperator(dialect, provider)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperTemplateStatementBuilder(
        dialect: JdbcDialect,
        dataOperator: JdbcDataOperator,
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
        dialect: JdbcDialect,
        clockProvider: ClockProvider,
        executionOptions: ExecutionOptions,
        logger: Logger,
        loggerFacade: LoggerFacade,
        session: JdbcSession,
        statementInspector: StatementInspector,
        dataFactory: JdbcDataFactory,
        dataOperator: JdbcDataOperator,
        dataSource: DataSource,
        templateStatementBuilder: TemplateStatementBuilder,
        statistics: StatisticManager,
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
            dataSource = dataSource,
            templateStatementBuilder = templateStatementBuilder,
            statisticManager = statistics,
        )
    }

    @Bean
    @ConditionalOnMissingBean
    open fun komapperDatabase(config: JdbcDatabaseConfig): JdbcDatabase {
        return JdbcDatabase(config)
    }
}
