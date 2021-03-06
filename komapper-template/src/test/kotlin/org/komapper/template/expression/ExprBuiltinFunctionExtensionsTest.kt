package org.komapper.template.expression

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ExprBuiltinFunctionExtensionsTest {

    private val extensions = ExprBuiltinFunctionExtensions { it }

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
