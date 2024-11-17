package org.komapper.spring.boot.autoconfigure.r2dbc

import org.assertj.core.api.Assertions.assertThat
import org.komapper.core.ClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Statement
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.Value
import org.komapper.dialect.h2.r2dbc.H2R2dbcDialect
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KomapperR2dbcAutoConfigurationTest {
    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                R2dbcAutoConfiguration::class.java,
                R2dbcTransactionManagerAutoConfiguration::class.java,
                KomapperR2dbcAutoConfiguration::class.java,
            )
        )
        .withPropertyValues(
            "spring.r2dbc.url=r2dbc:h2:mem:///test",
        )

    @Test
    fun defaultConfiguration() {
        contextRunner
            .run { context ->
                assertThat(context).hasNotFailed()

                val database = context.getBean(R2dbcDatabase::class.java)
                assertNotNull(database)
                assertTrue(database.config.dialect is H2R2dbcDialect)
                assertNotNull(database.config.templateStatementBuilder)
            }
    }

    @Test
    fun customConfiguration() {
        contextRunner
            .withUserConfiguration(CustomConfigure::class.java)
            .run { context ->
                assertThat(context).hasNotFailed()

                val database = context.getBean(R2dbcDatabase::class.java)
                assertNotNull(database)
                val clock = database.config.clockProvider.now()
                val timestamp = LocalDateTime.now(clock)
                assertEquals(LocalDateTime.of(2021, 4, 25, 16, 17, 18), timestamp)
                val executionOption = database.config.executionOptions
                assertEquals(1234, executionOption.fetchSize)
            }
    }

    @Test
    fun templateStatementBuilderConfiguration() {
        contextRunner
            .withUserConfiguration(TemplateStatementBuilderConfigure::class.java)
            .run { context ->
                assertThat(context).hasNotFailed()

                val database = context.getBean(R2dbcDatabase::class.java)
                assertNotNull(database)
                val builder = database.config.templateStatementBuilder
                assertTrue(builder is MyStatementBuilder)
            }
    }

    @Suppress("unused")
    @Configuration
    open class CustomConfigure {
        @Bean
        open fun clockProvider(): ClockProvider {
            return ClockProvider {
                val timestamp = LocalDateTime.of(2021, 4, 25, 16, 17, 18)
                Clock.fixed(timestamp.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
            }
        }

        @Bean
        open fun executionOption(): ExecutionOptions {
            return ExecutionOptions(fetchSize = 1234)
        }
    }

    @Suppress("unused")
    @Configuration
    open class TemplateStatementBuilderConfigure {
        @Bean
        open fun templateStatementBuilder(): TemplateStatementBuilder {
            return MyStatementBuilder()
        }
    }

    class MyStatementBuilder : TemplateStatementBuilder {
        override fun build(
            template: CharSequence,
            valueMap: Map<String, Value<*>>,
            builtinExtensions: TemplateBuiltinExtensions,
        ): Statement {
            throw UnsupportedOperationException()
        }

        override fun clearCache() = Unit
    }
}
