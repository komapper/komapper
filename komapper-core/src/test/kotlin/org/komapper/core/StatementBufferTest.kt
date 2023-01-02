package org.komapper.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
                StatementPart.Value(Value(1)),
                StatementPart.Text("bbb"),
                StatementPart.Value(Value(2)),
            ),
            buffer.parts,
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
                    StatementPart.Value(Value(2)),
                    StatementPart.Text("ccc"),
                ),
            ),
        )
        assertEquals(
            listOf(
                StatementPart.Text("aaa"),
                StatementPart.Value(Value(1)),
                StatementPart.Text("bbb"),
                StatementPart.Value(Value(2)),
                StatementPart.Text("ccc"),
            ),
            buffer.parts,
        )
    }

    @Test
    fun cutBack() {
        val buffer = StatementBuffer()
        buffer.append("abc")
        buffer.append("def")
        buffer.cutBack(2)
        val expected: List<StatementPart> = listOf(StatementPart.Text("abc"), StatementPart.Text("d"))
        assertEquals(expected, buffer.parts)
    }

    @Test
    fun cutBack_error_lengthIsTooLarge() {
        val buffer = StatementBuffer()
        buffer.append("abc")
        buffer.append("def")
        assertFailsWith<IllegalStateException> {
            buffer.cutBack(4)
        }
    }

    @Test
    fun cutBack_error_lastElementIsPlaceHolder() {
        val buffer = StatementBuffer()
        buffer.append("abc")
        buffer.bind(Value(1))
        assertFailsWith<IllegalStateException> {
            buffer.cutBack(1)
        }
    }
}
