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
        val location = SqlLocation(sql, 3, 4)
        assertEquals(
            """
            |[
            |select
            |  *
            |from
            |...^
            |  emp
            |]:3:4
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
        val location = SqlLocation(sql, 3, 0)
        assertEquals(
            """
            |[
            |select
            |  *
            |from
            |^
            |  emp
            |]:3:0
            """.trimMargin(),
            location.toString(),
        )
    }
}
