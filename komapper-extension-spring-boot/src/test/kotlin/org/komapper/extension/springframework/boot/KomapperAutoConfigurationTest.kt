package org.komapper.extension.springframework.boot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.komapper.core.Database
import org.komapper.core.jdbc.DataType
import org.komapper.core.jdbc.StringType
import org.komapper.jdbc.h2.H2Dialect
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource

class KomapperAutoConfigurationTest {

    private val context = AnnotationConfigApplicationContext()

    @Test
    fun test() {
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
    fun dataTypes() {
        val source = mapOf("spring.datasource.url" to "jdbc:h2:mem:example")
        val sources = context.environment.propertySources
        sources.addFirst(MapPropertySource("test", source))
        context.register(
            MyConfigure::class.java,
            KomapperAutoConfiguration::class.java,
            DataSourceAutoConfiguration::class.java
        )
        context.refresh()
        val database = context.getBean(Database::class.java)
        assertNotNull(database)
        val dataType = database.config.dialect.getDataType(String::class)
        assertEquals("abc", dataType.name)
    }

    @Configuration
    open class MyConfigure {

        @Bean
        open fun dataTypes(): Set<DataType<*>> {
            return setOf(StringType("abc"))
        }
    }
}
