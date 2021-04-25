package org.komapper.extension.springframework.boot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.komapper.core.ClockProvider
import org.komapper.core.Database
import org.komapper.core.data.JdbcOption
import org.komapper.core.jdbc.DataType
import org.komapper.core.jdbc.StringType
import org.komapper.jdbc.h2.H2Dialect
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
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
        assertTrue(database.config.dialect is H2Dialect)
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

        val jdbcOption = database.config.jdbcOption
        assertEquals(1234, jdbcOption.queryTimeoutSeconds)
    }

    @Suppress("unused")
    @Configuration
    open class CustomConfigure {

        @Bean
        open fun dataTypes(): Set<DataType<*>> {
            return setOf(StringType("abc"))
        }

        @Bean
        open fun clockProvider(): ClockProvider {
            return ClockProvider {
                val timestamp = LocalDateTime.of(2021, 4, 25, 16, 17, 18)
                Clock.fixed(timestamp.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
            }
        }

        @Bean
        open fun jdbcOption(): JdbcOption {
            return JdbcOption(queryTimeoutSeconds = 1234)
        }
    }
}
