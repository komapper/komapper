package org.komapper.extension.springframework.boot

import org.komapper.core.ClockProvider
import org.komapper.core.DataFactory
import org.komapper.core.Database
import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseSession
import org.komapper.core.DefaultClockProvider
import org.komapper.core.DefaultDataFactory
import org.komapper.core.Dialect
import org.komapper.core.JdbcOption
import org.komapper.core.Logger
import org.komapper.core.StdOutSqlLogger
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.jdbc.DataType
import org.komapper.core.spi.DefaultStatementInspector
import org.komapper.core.spi.LoggerFactory
import org.komapper.core.spi.StatementInspector
import org.komapper.core.spi.TemplateStatementBuilderFactory
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
    open fun dialect(environment: Environment, dataTypes: Set<DataType<*>> = emptySet()): Dialect {
        val url = environment.getProperty(DATASOURCE_URL_PROPERTY)
            ?: error(
                "$DATASOURCE_URL_PROPERTY is not found. " +
                    "Specify it to the application.properties file or define the dialect bean manually."
            )
        return Dialect.load(url, dataTypes)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun clockProvider(): ClockProvider {
        return DefaultClockProvider()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun jdbcOption(): JdbcOption {
        return JdbcOption()
    }

    @Bean
    @ConditionalOnMissingBean(Logger::class)
    open fun logger(): Logger {
        val loader = ServiceLoader.load(LoggerFactory::class.java)
        val factory = loader.firstOrNull()
        return factory?.create() ?: StdOutSqlLogger()
    }

    @Bean
    @ConditionalOnMissingBean(DatabaseSession::class)
    open fun databaseSession(dataSource: DataSource): DatabaseSession {
        return TransactionAwareDatabaseSession(dataSource)
    }

    @Bean
    @ConditionalOnMissingBean(StatementInspector::class)
    open fun statementInspector(): StatementInspector {
        val loader = ServiceLoader.load(StatementInspector::class.java)
        return loader.firstOrNull() ?: DefaultStatementInspector()
    }

    @Bean
    @ConditionalOnMissingBean(DataFactory::class)
    open fun dataFactory(databaseSession: DatabaseSession): DataFactory {
        return DefaultDataFactory(databaseSession)
    }

    @Bean
    @ConditionalOnMissingBean(DatabaseConfig::class)
    open fun databaseConfig(
        dialect: Dialect,
        clockProvider: ClockProvider,
        jdbcOption: JdbcOption,
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
            override val jdbcOption = jdbcOption
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

    private fun loadTemplateStatementBuilder(dialect: Dialect): TemplateStatementBuilder {
        val loader = ServiceLoader.load(TemplateStatementBuilderFactory::class.java)
        val factory = loader.firstOrNull()
            ?: error(
                "TemplateStatementBuilderFactory is not found. " +
                    "Add komapper-template dependency or define the TemplateStatementBuilder bean."
            )
        return factory.create(dialect)
    }

    @Bean
    @ConditionalOnMissingBean(Database::class)
    open fun database(config: DatabaseConfig): Database {
        return Database.create(config)
    }
}
