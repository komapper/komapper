package org.komapper.core

import kotlin.test.Test
import kotlin.test.assertEquals

internal class TemplateBuiltinExtensionsTest {
    private val extensions = TemplateBuiltinExtensions { it }

    @Test
    fun escape() {
        with(extensions) {
            assertEquals("abc", "abc".escape())
        }
    }

    @Test
    fun asPrefix() {
        with(extensions) {
            assertEquals("abc%", "abc".asPrefix())
        }
    }

    @Test
    fun asInfix() {
        with(extensions) {
            assertEquals("%abc%", "abc".asInfix())
        }
    }

    @Test
    fun asSuffix() {
        with(extensions) {
            assertEquals("%abc", "abc".asSuffix())
        }
    }
}
