package org.komapper.spring.boot.autoconfigure.jdbc

import org.komapper.core.ClockProvider
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.StdOutLogger
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.spi.DefaultStatementInspector
import org.komapper.core.spi.LoggerFactory
import org.komapper.core.spi.StatementInspector
import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.jdbc.DefaultJdbcDataFactory
import org.komapper.jdbc.JdbcDataFactory
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcSession
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.util.Optional
import java.util.ServiceLoader
import java.util.UUID
import javax.sql.DataSource

@Suppress("unused")
@Configuration
@ConditionalOnClass(JdbcDatabase::class)
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
open class KomapperAutoConfiguration {

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
        return JdbcDialect.load(url, dataTypes ?: emptyList())
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
        val loader = ServiceLoader.load(LoggerFactory::class.java)
        val factory = loader.firstOrNull()
        return factory?.create() ?: StdOutLogger()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun databaseSession(dataSource: DataSource): JdbcSession {
        return TransactionAwareSession(dataSource)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun statementInspector(): StatementInspector {
        val loader = ServiceLoader.load(StatementInspector::class.java)
        return loader.firstOrNull() ?: DefaultStatementInspector()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun dataFactory(databaseSession: JdbcSession): JdbcDataFactory {
        return DefaultJdbcDataFactory(databaseSession)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun databaseConfig(
        dialect: JdbcDialect,
        clockProvider: ClockProvider,
        executionOptions: ExecutionOptions,
        logger: Logger,
        session: JdbcSession,
        statementInspector: StatementInspector,
        dataFactory: JdbcDataFactory,
        templateStatementBuilder: Optional<TemplateStatementBuilder>
    ): JdbcDatabaseConfig {
        return object : JdbcDatabaseConfig {
            override val id = UUID.randomUUID()
            override val dialect = dialect
            override val clockProvider = clockProvider
            override val executionOptions = executionOptions
            override val logger = logger
            override val session = session
            override val statementInspector = statementInspector
            override val dataFactory = dataFactory
            override val templateStatementBuilder by lazy {
                templateStatementBuilder.orElseGet {
                    loadTemplateStatementBuilder(dialect)
                }
            }
        }
    }

    private fun loadTemplateStatementBuilder(dialect: JdbcDialect): TemplateStatementBuilder {
        val loader = ServiceLoader.load(TemplateStatementBuilderFactory::class.java)
        val factory = loader.firstOrNull()
            ?: error(
                "TemplateStatementBuilderFactory is not found. " +
                    "Add komapper-template dependency or define the TemplateStatementBuilder bean."
            )
        return factory.create(dialect)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun database(config: JdbcDatabaseConfig): JdbcDatabase {
        return JdbcDatabase.create(config)
    }
}
