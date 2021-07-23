package org.komapper.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class StatementBufferTest {

    @Test
    fun appendAndBind() {
        val buffer = StatementBuffer()
        buffer.append("aaa")
        buffer.bind(Value(1))
        buffer.append("bbb")
        buffer.bind(Value(2))
        assertEquals(
            listOf(
                StatementPart.Text("aaa"),
                StatementPart.PlaceHolder(Value(1)),
                StatementPart.Text("bbb"),
                StatementPart.PlaceHolder(Value(2))
            ),
            buffer.parts
        )
    }

    @Test
    fun appendStatement() {
        val buffer = StatementBuffer()
        buffer.append("aaa")
        buffer.bind(Value(1))
        buffer.append(
            Statement(
                listOf(
                    StatementPart.Text("bbb"),
                    StatementPart.PlaceHolder(Value(2)),
                    StatementPart.Text("ccc")
                )
            )
        )
        assertEquals(
            listOf(
                StatementPart.Text("aaa"),
                StatementPart.PlaceHolder(Value(1)),
                StatementPart.Text("bbb"),
                StatementPart.PlaceHolder(Value(2)),
                StatementPart.Text("ccc"),
            ),
            buffer.parts
        )
    }

    @Test
    fun cutBack() {
        val buffer = StatementBuffer()
        buffer.append("abc")
        buffer.append("def")
        buffer.cutBack(2)
        assertEquals(listOf(StatementPart.Text("abc"), StatementPart.Text("d")), buffer.parts)
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
