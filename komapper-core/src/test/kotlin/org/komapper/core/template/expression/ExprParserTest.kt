package org.komapper.core.template.expression

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExprParserTest {
    @Test
    fun classRef() {
        when (val expr = ExprParser("@aaa.bbb.Ccc@").parse()) {
            is ExprNode.ClassRef -> {
                assertEquals("aaa.bbb.Ccc", expr.name)
            }

            else -> {
                throw AssertionError(expr)
            }
        }
    }

    @Test
    fun gt() {
        when (val expr = ExprParser("aaa > 1").parse()) {
            is ExprNode.Gt -> {
                assertTrue(expr.left is ExprNode.Value)
                assertTrue(expr.right is ExprNode.Literal)
            }

            else -> {
                throw AssertionError()
            }
        }
    }

    @Test
    fun `is`() {
        when (val expr = ExprParser("aaa is @bbb.Ccc@").parse()) {
            is ExprNode.Is -> {
                assertTrue(expr.left is ExprNode.Value)
                assertTrue(expr.right is ExprNode.ClassRef)
            }

            else -> {
                throw AssertionError()
            }
        }
    }

    @Test
    fun and() {
        when (val expr = ExprParser("aaa > 1 && true").parse()) {
            is ExprNode.And -> {
                assertTrue(expr.left is ExprNode.Gt)
                assertTrue(expr.right is ExprNode.Literal)
            }

            else -> {
                throw AssertionError()
            }
        }
    }

    @Test
    fun property() {
        when (val expr = ExprParser("aaa.age").parse()) {
            is ExprNode.Property -> {
                assertEquals("age", expr.name)
                assertTrue(expr.receiver is ExprNode.Value)
                assertEquals("aaa", (expr.receiver as ExprNode.Value).name)
            }

            else -> {
                throw AssertionError()
            }
        }
    }

    @Test
    fun nestedProperty() {
        when (val expr = ExprParser("aaa.age.int").parse()) {
            is ExprNode.Property -> {
                assertEquals("int", expr.name)
                val parent = expr.receiver
                assertTrue(parent is ExprNode.Property)
                assertEquals("age", parent.name)
                val grandParent = parent.receiver
                assertTrue(grandParent is ExprNode.Value)
                assertEquals("aaa", grandParent.name)
            }

            else -> {
                throw AssertionError()
            }
        }
    }

    @Test
    fun function() {
        when (val expr = ExprParser("aaa.hello(1, 2, 3)").parse()) {
            is ExprNode.Function -> {
                assertEquals("hello", expr.name)
                assertTrue(expr.receiver is ExprNode.Value)
                assertEquals("aaa", (expr.receiver as ExprNode.Value).name)
            }

            else -> {
                throw AssertionError()
            }
        }
    }

    @Test
    fun topLevelFunction() {
        when (val expr = ExprParser("hello()").parse()) {
            is ExprNode.CallableValue -> {
                assertEquals("hello", expr.name)
                assertTrue(expr.args is ExprNode.Empty)
            }

            else -> {
                throw AssertionError()
            }
        }
    }

    @Test
    fun nestedFunction() {
        when (val expr = ExprParser("aaa.hello(1, 2, 3).bye(4)").parse()) {
            is ExprNode.Function -> {
                assertEquals("bye", expr.name)
                assertTrue(expr.args is ExprNode.Literal)
                assertEquals(4, (expr.args as ExprNode.Literal).value)
                val parent = expr.receiver
                assertTrue(parent is ExprNode.Function)
                assertEquals("hello", parent.name)
                assertTrue(parent.args is ExprNode.Comma)
                assertEquals(3, (parent.args as ExprNode.Comma).nodeList.size)
                val grandParent = parent.receiver
                assertTrue(grandParent is ExprNode.Value)
                assertEquals("aaa", grandParent.name)
            }

            else -> {
                throw AssertionError()
            }
        }
    }

    @Test
    fun comma() {
        when (val expr = ExprParser("a, b, c").parse()) {
            is ExprNode.Comma -> {
                assertEquals(3, expr.nodeList.size)
            }

            else -> {
                throw AssertionError()
            }
        }
    }

    @Test
    fun `The operand is not found`() {
        val exception = assertFailsWith<ExprException> { ExprParser("aaa >").parse() }
        println(exception)
    }

    @Test
    fun `The illegal number literal is found`() {
        val exception = assertFailsWith<ExprException> {
            ExprParser("1 + 1a")
                .parse()
        }
        println(exception)
    }

    @Test
    fun `The close paren is not found`() {
        val exception =
            assertFailsWith<ExprException> { ExprParser("aaa(bbb").parse() }
        println(exception)
    }

    @Test
    fun `The token is not supported`() {
        val exception =
            assertFailsWith<ExprException> { ExprParser("aaa * bbb").parse() }
        println(exception)
    }
}
