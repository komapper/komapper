package org.komapper.jdbc.spring.boot.autoconfigure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.core.ClockProvider
import org.komapper.core.ExecutionOption
import org.komapper.core.Statement
import org.komapper.core.TemplateStatementBuilder
import org.komapper.jdbc.DataType
import org.komapper.jdbc.Database
import org.komapper.jdbc.StringType
import org.komapper.jdbc.dialect.h2.H2JdbcDialect
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import java.lang.IllegalStateException
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class KomapperAutoConfigurationTest {

    private val context = AnnotationConfigApplicationContext()

    @Test
    fun defaultConfiguration() {
        val source = mapOf("spring.datasource.url" to "jdbc:h2:mem:example")
        val sources = context.environment.propertySources
        sources.addFirst(MapPropertySource("test", source))
        context.register(
            KomapperAutoConfiguration::class.java,
            DataSourceAutoConfiguration::class.java
        )
        context.refresh()

        val database = context.getBean(Database::class.java)
        assertNotNull(database)
        assertTrue(database.config.dialect is H2JdbcDialect)
        assertThrows<IllegalStateException> {
            database.config.templateStatementBuilder
        }
    }

    @Test
    fun customConfiguration() {
        val source = mapOf("spring.datasource.url" to "jdbc:h2:mem:example")
        val sources = context.environment.propertySources
        sources.addFirst(MapPropertySource("test", source))
        context.register(
            CustomConfigure::class.java,
            KomapperAutoConfiguration::class.java,
            DataSourceAutoConfiguration::class.java
        )
        context.refresh()

        val database = context.getBean(Database::class.java)
        assertNotNull(database)
        val dataType = database.config.dialect.getDataType(String::class)
        assertEquals("abc", dataType.name)
        val clock = database.config.clockProvider.now()
        val timestamp = LocalDateTime.now(clock)
        assertEquals(LocalDateTime.of(2021, 4, 25, 16, 17, 18), timestamp)
        val jdbcOption = database.config.executionOption
        assertEquals(1234, jdbcOption.queryTimeoutSeconds)
    }

    @Test
    fun templateStatementBuilderConfiguration() {
        val source = mapOf("spring.datasource.url" to "jdbc:h2:mem:example")
        val sources = context.environment.propertySources
        sources.addFirst(MapPropertySource("test", source))
        context.register(
            TemplateStatementBuilderConfigure::class.java,
            KomapperAutoConfiguration::class.java,
            DataSourceAutoConfiguration::class.java
        )
        context.refresh()

        val database = context.getBean(Database::class.java)
        assertNotNull(database)
        val builder = database.config.templateStatementBuilder
        assertTrue(builder is MyStatementBuilder)
    }

    @Suppress("unused")
    @Configuration
    open class CustomConfigure {

        @Bean
        open fun dataTypes(): List<DataType<*>> {
            return listOf(StringType("abc"))
        }

        @Bean
        open fun clockProvider(): ClockProvider {
            return ClockProvider {
                val timestamp = LocalDateTime.of(2021, 4, 25, 16, 17, 18)
                Clock.fixed(timestamp.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
            }
        }

        @Bean
        open fun jdbcOption(): ExecutionOption {
            return ExecutionOption(queryTimeoutSeconds = 1234)
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
        override fun build(template: CharSequence, params: Any, escape: (String) -> String): Statement {
            throw UnsupportedOperationException()
        }

        override fun clearCache() = Unit
    }
}
