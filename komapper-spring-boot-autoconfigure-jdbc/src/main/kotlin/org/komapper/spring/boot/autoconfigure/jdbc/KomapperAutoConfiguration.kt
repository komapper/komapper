package org.komapper.spring.boot.autoconfigure.jdbc

import org.komapper.core.ClockProvider
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOption
import org.komapper.core.Logger
import org.komapper.core.StdOutLogger
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.spi.DefaultStatementInspector
import org.komapper.core.spi.LoggerFactory
import org.komapper.core.spi.StatementInspector
import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.jdbc.DataFactory
import org.komapper.jdbc.DataType
import org.komapper.jdbc.Database
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.DatabaseSession
import org.komapper.jdbc.DefaultDataFactory
import org.komapper.jdbc.JdbcDialect
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
@ConditionalOnClass(Database::class)
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
open class KomapperAutoConfiguration {

    companion object {
        private const val DATASOURCE_URL_PROPERTY = "spring.datasource.url"
    }

    @Bean
    @ConditionalOnMissingBean
    open fun jdbcDialect(environment: Environment, dataTypes: List<DataType<*>>?): JdbcDialect {
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
    open fun executionOption(): ExecutionOption {
        return ExecutionOption()
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
    open fun databaseSession(dataSource: DataSource): DatabaseSession {
        return TransactionAwareDatabaseSession(dataSource)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun statementInspector(): StatementInspector {
        val loader = ServiceLoader.load(StatementInspector::class.java)
        return loader.firstOrNull() ?: DefaultStatementInspector()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun dataFactory(databaseSession: DatabaseSession): DataFactory {
        return DefaultDataFactory(databaseSession)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun databaseConfig(
        dialect: JdbcDialect,
        clockProvider: ClockProvider,
        executionOption: ExecutionOption,
        logger: Logger,
        session: DatabaseSession,
        statementInspector: StatementInspector,
        dataFactory: DataFactory,
        templateStatementBuilder: Optional<TemplateStatementBuilder>
    ): DatabaseConfig {
        return object : DatabaseConfig {
            override val id = UUID.randomUUID()
            override val dialect = dialect
            override val clockProvider = clockProvider
            override val executionOption = executionOption
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
    open fun database(config: DatabaseConfig): Database {
        return Database.create(config)
    }
}
