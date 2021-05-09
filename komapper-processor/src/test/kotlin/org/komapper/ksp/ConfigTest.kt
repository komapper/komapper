package org.komapper.ksp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.core.CamelToLowerSnakeCase
import org.komapper.core.Implicit

class ConfigTest {

    @Test
    fun empty() {
        val options = emptyMap<String, String>()
        val config = Config.create(options)
        assertEquals("_", config.prefix)
        assertEquals("", config.suffix)
        assertEquals(CamelToLowerSnakeCase, config.namingStrategy)
    }

    @Test
    fun prefix() {
        val options = mapOf("komapper.prefix" to "A")
        val config = Config.create(options)
        assertEquals("A", config.prefix)
    }

    @Test
    fun suffix() {
        val options = mapOf("komapper.suffix" to "A")
        val config = Config.create(options)
        assertEquals("A", config.suffix)
    }

    @Test
    fun namingStrategy() {
        val options = mapOf("komapper.namingStrategy" to "implicit")
        val config = Config.create(options)
        assertEquals(Implicit, config.namingStrategy)
    }

    @Test
    fun namingStrategy_error() {
        val options = mapOf("komapper.namingStrategy" to "unknown")
        val e = assertThrows<IllegalStateException> {
            Config.create(options)
        }
        assertEquals("'unknown' is illegal value as a komapper.namingStrategy option.", e.message)
    }
}
