package org.komapper.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException

internal class StatementBufferTest {

    @Test
    fun appendAndBind() {
        val buffer = StatementBuffer()
        buffer.append("aaa")
        buffer.bind(Value(1))
        buffer.append("bbb")
        buffer.bind(Value(2))
        assertEquals(listOf("aaa", PlaceHolder, "bbb", PlaceHolder), buffer.charSequences)
        assertEquals(listOf(Value(1), Value(2)), buffer.args)
    }

    @Test
    fun appendStatement() {
        val buffer = StatementBuffer()
        buffer.append("aaa")
        buffer.bind(Value(1))
        buffer.append(Statement(listOf("bbb", PlaceHolder, "ccc"), listOf(Value(2))))
        assertEquals(listOf("aaa", PlaceHolder, "bbb", PlaceHolder, "ccc"), buffer.charSequences)
        assertEquals(listOf(Value(1), Value(2)), buffer.args)
    }

    @Test
    fun cutBack() {
        val buffer = StatementBuffer()
        buffer.append("abc")
        buffer.append("def")
        buffer.cutBack(2)
        assertEquals(listOf("abc", "d"), buffer.charSequences)
    }

    @Test
    fun cutBack_error_lengthIsTooLarge() {
        val buffer = StatementBuffer()
        buffer.append("abc")
        buffer.append("def")
        assertThrows<IllegalStateException> {
            buffer.cutBack(4)
        }
    }

    @Test
    fun cutBack_error_lastElementIsPlaceHolder() {
        val buffer = StatementBuffer()
        buffer.append("abc")
        buffer.bind(Value(1))
        assertThrows<IllegalStateException> {
            buffer.cutBack(1)
        }
    }


}