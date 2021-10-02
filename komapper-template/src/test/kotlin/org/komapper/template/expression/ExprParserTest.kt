package org.komapper.template.expression

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
            else -> throw AssertionError(expr)
        }
    }

    @Test
    fun gt() {
        when (val expr = ExprParser("aaa > 1").parse()) {
            is ExprNode.Gt -> {
                assertTrue(expr.left is ExprNode.Value)
                assertTrue(expr.right is ExprNode.Literal)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun and() {
        when (val expr = ExprParser("aaa > 1 && true").parse()) {
            is ExprNode.And -> {
                assertTrue(expr.left is ExprNode.Gt)
                assertTrue(expr.right is ExprNode.Literal)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun property() {
        when (val expr = ExprParser("aaa.age").parse()) {
            is ExprNode.Property -> {
                assertEquals(expr.name, "age")
                assertTrue(expr.receiver is ExprNode.Value)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun comma() {
        when (val expr = ExprParser("a, b, c").parse()) {
            is ExprNode.Comma -> {
                assertEquals(3, expr.nodeList.size)
            }
            else -> throw AssertionError()
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
