package org.komapper.spring.boot.autoconfigure.r2dbc

import org.komapper.core.ClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Statement
import org.komapper.core.TemplateStatementBuilder
import org.komapper.dialect.h2.r2dbx.H2R2dbcDialect
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KomapperAutoConfigurationTest {

    private val context = AnnotationConfigApplicationContext()

    @Test
    fun defaultConfiguration() {
        val source = mapOf("spring.r2dbc.url" to "r2dbc:h2:mem:///test")
        val sources = context.environment.propertySources
        sources.addFirst(MapPropertySource("test", source))
        context.register(
            KomapperAutoConfiguration::class.java,
            R2dbcAutoConfiguration::class.java
        )
        context.refresh()

        val database = context.getBean(R2dbcDatabase::class.java)
        assertNotNull(database)
        assertTrue(database.config.dialect is H2R2dbcDialect)
        assertFailsWith<IllegalStateException> {
            database.config.templateStatementBuilder
        }
    }

    @Test
    fun customConfiguration() {
        val source = mapOf("spring.r2dbc.url" to "r2dbc:h2:mem:///test")
        val sources = context.environment.propertySources
        sources.addFirst(MapPropertySource("test", source))
        context.register(
            CustomConfigure::class.java,
            KomapperAutoConfiguration::class.java,
            R2dbcAutoConfiguration::class.java
        )
        context.refresh()

        val database = context.getBean(R2dbcDatabase::class.java)
        assertNotNull(database)
        val clock = database.config.clockProvider.now()
        val timestamp = LocalDateTime.now(clock)
        assertEquals(LocalDateTime.of(2021, 4, 25, 16, 17, 18), timestamp)
        val executionOption = database.config.executionOptions
        assertEquals(1234, executionOption.fetchSize)
    }

    @Test
    fun templateStatementBuilderConfiguration() {
        val source = mapOf("spring.r2dbc.url" to "r2dbc:h2:mem:///test")
        val sources = context.environment.propertySources
        sources.addFirst(MapPropertySource("test", source))
        context.register(
            TemplateStatementBuilderConfigure::class.java,
            KomapperAutoConfiguration::class.java,
            R2dbcAutoConfiguration::class.java
        )
        context.refresh()

        val database = context.getBean(R2dbcDatabase::class.java)
        assertNotNull(database)
        val builder = database.config.templateStatementBuilder
        assertTrue(builder is MyStatementBuilder)
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
        override fun build(template: CharSequence, data: Any, escape: (String) -> String): Statement {
            throw UnsupportedOperationException()
        }

        override fun clearCache() = Unit
    }
}
