package org.komapper.spring.boot.autoconfigure.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.ClockProvider
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.StatementInspector
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.spi.LoggerProvider
import org.komapper.core.spi.StatementInspectorProvider
import org.komapper.core.spi.TemplateStatementBuilderProvider
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcSession
import org.komapper.r2dbc.spi.R2dbcDialectProvider
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.util.Optional
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
        val driver = R2dbcDialectProvider.extractR2dbcDriver(url)
        return R2dbcDialectProvider.get(driver)
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
        return LoggerProvider.get()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun r2dbcDatabaseSession(connectionFactory: ConnectionFactory): R2dbcSession {
        return TransactionAwareSession(connectionFactory)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun statementInspector(): StatementInspector {
        return StatementInspectorProvider.get()
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
        session: R2dbcSession,
        statementInspector: StatementInspector,
        templateStatementBuilder: Optional<TemplateStatementBuilder>
    ): R2dbcDatabaseConfig {
        return object : R2dbcDatabaseConfig {
            override val id = UUID.randomUUID()
            override val dialect = r2dbcDialect
            override val clockProvider = clockProvider
            override val executionOptions = executionOptions
            override val logger = logger
            override val session = session
            override val statementInspector = statementInspector
            override val templateStatementBuilder by lazy {
                templateStatementBuilder.orElseGet {
                    loadTemplateStatementBuilder(r2dbcDialect)
                }
            }
        }
    }

    private fun loadTemplateStatementBuilder(dialect: R2dbcDialect): TemplateStatementBuilder {
        return TemplateStatementBuilderProvider.get(dialect)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun r2dbcDatabase(config: R2dbcDatabaseConfig): R2dbcDatabase {
        return R2dbcDatabase.create(config)
    }
}
