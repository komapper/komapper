package org.komapper.spring.boot.autoconfigure.jdbc

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
import org.komapper.jdbc.DefaultJdbcDataFactory
import org.komapper.jdbc.JdbcDataFactory
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcDialects
import org.komapper.jdbc.JdbcSession
import org.komapper.spring.jdbc.JdbcTransactionAwareSession
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.transaction.PlatformTransactionManager
import java.util.UUID
import javax.sql.DataSource

@Suppress("unused")
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JdbcDatabase::class)
@ImportAutoConfiguration(value = [DataSourceAutoConfiguration::class, DataSourceTransactionManagerAutoConfiguration::class])
open class JdbcKomapperAutoConfiguration {

    companion object {
        private const val DATASOURCE_URL_PROPERTY = "spring.datasource.url"
    }

    @Bean
    @ConditionalOnMissingBean
    open fun jdbcDialect(environment: Environment, dataTypes: List<JdbcDataType<*>>?): JdbcDialect {
        val url = environment.getProperty(DATASOURCE_URL_PROPERTY)
            ?: error(
                "$DATASOURCE_URL_PROPERTY is not found. " +
                    "Specify it to the application.properties file or define the Dialect bean manually."
            )
        return JdbcDialects.getByUrl(url, dataTypes ?: emptyList())
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
    open fun databaseSession(transactionManager: PlatformTransactionManager, dataSource: DataSource): JdbcSession {
        return JdbcTransactionAwareSession(transactionManager, dataSource)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun statementInspector(): StatementInspector {
        return StatementInspectors.get()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun dataFactory(databaseSession: JdbcSession): JdbcDataFactory {
        return DefaultJdbcDataFactory(databaseSession)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun templateStatementBuilder(dialect: JdbcDialect): TemplateStatementBuilder {
        return TemplateStatementBuilders.get(dialect)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun databaseConfig(
        dialect: JdbcDialect,
        clockProvider: ClockProvider,
        executionOptions: ExecutionOptions,
        logger: Logger,
        loggerFacade: LoggerFacade,
        session: JdbcSession,
        statementInspector: StatementInspector,
        dataFactory: JdbcDataFactory,
        templateStatementBuilder: TemplateStatementBuilder
    ): JdbcDatabaseConfig {
        return object : JdbcDatabaseConfig {
            override val id = UUID.randomUUID()
            override val dialect = dialect
            override val clockProvider = clockProvider
            override val executionOptions = executionOptions
            override val logger = logger
            override val loggerFacade = loggerFacade
            override val session = session
            override val statementInspector = statementInspector
            override val dataFactory = dataFactory
            override val templateStatementBuilder = templateStatementBuilder
        }
    }

    @Bean
    @ConditionalOnMissingBean
    open fun database(config: JdbcDatabaseConfig): JdbcDatabase {
        return JdbcDatabase.create(config)
    }
}
