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
        val location = SqlLocation(sql, 3, 0, 2, 4)
        assertEquals(
            """
            |[
            |select
            |  *
            |fr>>>om<<<
            |  emp
            |]:3:3
            """.trimMargin(),
            location.toString(),
        )
    }

    @Test
    fun `startColumnIndex is 0`() {
        val sql = """
            |select
            |  *
            |from
            |  emp
        """.trimMargin()
        val location = SqlLocation(sql, 3, 0, 0, 4)
        assertEquals(
            """
            |[
            |select
            |  *
            |>>>from<<<
            |  emp
            |]:3:1
            """.trimMargin(),
            location.toString(),
        )
    }
}
