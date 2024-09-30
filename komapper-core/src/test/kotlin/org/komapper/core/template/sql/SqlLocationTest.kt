package org.komapper.core.template.sql

import kotlin.test.Test
import kotlin.test.assertEquals

class SqlLocationTest {

    @Test
    fun test() {
        val sql = """
            |select
            |  *
            |from
            |  emp
        """.trimMargin()
        val location = SqlLocation(sql, 3, 2, 4)
        assertEquals(
            """
            |[
            |select
            |  *
            |fr>>>om<<<
            |  emp
            |]:3:2..4
            """.trimMargin(),
            location.toString(),
        )
    }

    @Test
    fun `position is 0`() {
        val sql = """
            |select
            |  *
            |from
            |  emp
        """.trimMargin()
        val location = SqlLocation(sql, 3, 0, 4)
        assertEquals(
            """
            |[
            |select
            |  *
            |>>>from<<<
            |  emp
            |]:3:0..4
            """.trimMargin(),
            location.toString(),
        )
    }
}
