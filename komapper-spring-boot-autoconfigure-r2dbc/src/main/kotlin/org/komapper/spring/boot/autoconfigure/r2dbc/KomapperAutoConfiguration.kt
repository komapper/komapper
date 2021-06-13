package org.komapper.spring.boot.autoconfigure.r2dbc

import io.r2dbc.spi.ConnectionFactory
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
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseSession
import org.komapper.r2dbc.R2dbcDialect
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.util.Optional
import java.util.ServiceLoader
import java.util.UUID

@Suppress("unused")
@Configuration
@ConditionalOnClass(R2dbcDatabase::class)
@AutoConfigureAfter(R2dbcAutoConfiguration::class)
open class KomapperAutoConfiguration {

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
        val driver = R2dbcDialect.extractR2dbcDriver(url)
        return R2dbcDialect.load(driver)
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
    open fun r2dbcDatabaseSession(connectionFactory: ConnectionFactory): R2dbcDatabaseSession {
        return TransactionAwareDatabaseSession(connectionFactory)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun statementInspector(): StatementInspector {
        val loader = ServiceLoader.load(StatementInspector::class.java)
        return loader.firstOrNull() ?: DefaultStatementInspector()
    }

//    @Bean
//    @ConditionalOnMissingBean
//    open fun dataFactory(databaseSession: DatabaseSession): DataFactory {
//        return DefaultDataFactory(databaseSession)
//    }

    @Bean
    @ConditionalOnMissingBean
    open fun databaseConfig(
        r2dbcDialect: R2dbcDialect,
        clockProvider: ClockProvider,
        executionOptions: ExecutionOptions,
        logger: Logger,
        r2dbcDatabaseSession: R2dbcDatabaseSession,
        statementInspector: StatementInspector,
        templateStatementBuilder: Optional<TemplateStatementBuilder>
    ): R2dbcDatabaseConfig {
        return object : R2dbcDatabaseConfig {
            override val id = UUID.randomUUID()
            override val dialect = r2dbcDialect
            override val clockProvider = clockProvider
            override val executionOptions = executionOptions
            override val logger = logger
            override val session = r2dbcDatabaseSession
            override val statementInspector = statementInspector
            override val templateStatementBuilder by lazy {
                templateStatementBuilder.orElseGet {
                    loadTemplateStatementBuilder(r2dbcDialect)
                }
            }
        }
    }

    private fun loadTemplateStatementBuilder(dialect: R2dbcDialect): TemplateStatementBuilder {
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
    open fun r2dbcDatabase(config: R2dbcDatabaseConfig): R2dbcDatabase {
        return R2dbcDatabase.create(config)
    }
}
