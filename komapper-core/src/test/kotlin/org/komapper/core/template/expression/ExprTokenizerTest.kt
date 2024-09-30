package org.komapper.core.template.expression

import org.komapper.core.template.expression.ExprTokenType.AND
import org.komapper.core.template.expression.ExprTokenType.BIG_DECIMAL
import org.komapper.core.template.expression.ExprTokenType.CLASS_REF
import org.komapper.core.template.expression.ExprTokenType.DOUBLE
import org.komapper.core.template.expression.ExprTokenType.EOE
import org.komapper.core.template.expression.ExprTokenType.FALSE
import org.komapper.core.template.expression.ExprTokenType.FLOAT
import org.komapper.core.template.expression.ExprTokenType.INT
import org.komapper.core.template.expression.ExprTokenType.LONG
import org.komapper.core.template.expression.ExprTokenType.NULL
import org.komapper.core.template.expression.ExprTokenType.PROPERTY
import org.komapper.core.template.expression.ExprTokenType.SAFE_CALL_PROPERTY
import org.komapper.core.template.expression.ExprTokenType.STRING
import org.komapper.core.template.expression.ExprTokenType.TRUE
import org.komapper.core.template.expression.ExprTokenType.VALUE
import org.komapper.core.template.expression.ExprTokenType.WHITESPACE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExprTokenizerTest {

    @Test
    fun value() {
        val tokenizer = ExprTokenizer("name")
        assertEquals(VALUE, tokenizer.next())
        assertEquals("name", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun classRef() {
        val tokenizer = ExprTokenizer("@aaa.bbb.Ccc@")
        assertEquals(CLASS_REF, tokenizer.next())
        assertEquals("@aaa.bbb.Ccc@", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun stringLiteral() {
        val tokenizer = ExprTokenizer("\"aaa bbb\"")
        assertEquals(STRING, tokenizer.next())
        assertEquals("\"aaa bbb\"", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun intLiteral() {
        val tokenizer = ExprTokenizer("+13")
        assertEquals(INT, tokenizer.next())
        assertEquals("+13", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun longLiteral() {
        val tokenizer = ExprTokenizer("+13L")
        assertEquals(LONG, tokenizer.next())
        assertEquals("+13L", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun floatLiteral() {
        val tokenizer = ExprTokenizer("+13F")
        assertEquals(FLOAT, tokenizer.next())
        assertEquals("+13F", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun doubleLiteral() {
        val tokenizer = ExprTokenizer("+13D")
        assertEquals(DOUBLE, tokenizer.next())
        assertEquals("+13D", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun bigDecimalLiteral() {
        val tokenizer = ExprTokenizer("+13B")
        assertEquals(BIG_DECIMAL, tokenizer.next())
        assertEquals("+13B", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun nullLiteral() {
        val tokenizer = ExprTokenizer("null")
        assertEquals(NULL, tokenizer.next())
        assertEquals("null", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun trueLiteral() {
        val tokenizer = ExprTokenizer("true")
        assertEquals(TRUE, tokenizer.next())
        assertEquals("true", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun falseLiteral() {
        val tokenizer = ExprTokenizer("false")
        assertEquals(FALSE, tokenizer.next())
        assertEquals("false", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun expressions() {
        val tokenizer = ExprTokenizer("manager.aaa && name.bbb")
        assertEquals(VALUE, tokenizer.next())
        assertEquals("manager", tokenizer.token)
        assertEquals(PROPERTY, tokenizer.next())
        assertEquals(".aaa", tokenizer.token)
        assertEquals(WHITESPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(AND, tokenizer.next())
        assertEquals("&&", tokenizer.token)
        assertEquals(WHITESPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(VALUE, tokenizer.next())
        assertEquals("name", tokenizer.token)
        assertEquals(PROPERTY, tokenizer.next())
        assertEquals(".bbb", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun index() {
        val tokenizer = ExprTokenizer("aaa bbb ccc")
        assertEquals(0, tokenizer.location.startIndex)
        assertEquals(0, tokenizer.location.endIndex)
        assertEquals(VALUE, tokenizer.next())
        assertEquals("aaa", tokenizer.token)
        assertEquals(0, tokenizer.location.startIndex)
        assertEquals(3, tokenizer.location.endIndex)
        assertEquals(WHITESPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(3, tokenizer.location.startIndex)
        assertEquals(4, tokenizer.location.endIndex)
        assertEquals(VALUE, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(4, tokenizer.location.startIndex)
        assertEquals(7, tokenizer.location.endIndex)
        assertEquals(WHITESPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(7, tokenizer.location.startIndex)
        assertEquals(8, tokenizer.location.endIndex)
        assertEquals(VALUE, tokenizer.next())
        assertEquals("ccc", tokenizer.token)
        assertEquals(8, tokenizer.location.startIndex)
        assertEquals(11, tokenizer.location.endIndex)
    }

    @Test
    fun property() {
        val tokenizer = ExprTokenizer("aaa.bbb")
        assertEquals(VALUE, tokenizer.next())
        assertEquals("aaa", tokenizer.token)
        assertEquals(PROPERTY, tokenizer.next())
        assertEquals(".bbb", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun safeCallProperty() {
        val tokenizer = ExprTokenizer("aaa?.bbb")
        assertEquals(VALUE, tokenizer.next())
        assertEquals("aaa", tokenizer.token)
        assertEquals(SAFE_CALL_PROPERTY, tokenizer.next())
        assertEquals("?.bbb", tokenizer.token)
        assertEquals(EOE, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun `The end of single quotation mark is not found`() {
        val tokenizer = ExprTokenizer("'aaa")
        val exception = assertFailsWith<ExprException> { tokenizer.next() }
        println(exception)
    }

    @Test
    fun `The end of double quotation mark is not found`() {
        val tokenizer = ExprTokenizer("\"aaa")
        val exception = assertFailsWith<ExprException> { tokenizer.next() }
        println(exception)
    }

    @Test
    fun `Either property or function name must follow the dot`() {
        val tokenizer = ExprTokenizer(".")
        val exception = assertFailsWith<ExprException> { tokenizer.next() }
        println(exception)
    }

    @Test
    fun `The character is illegal as an identifier start`() {
        val tokenizer = ExprTokenizer(".!")
        val exception = assertFailsWith<ExprException> { tokenizer.next() }
        println(exception)
    }
}
