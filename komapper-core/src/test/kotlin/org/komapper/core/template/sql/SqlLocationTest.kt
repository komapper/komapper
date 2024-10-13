package org.komapper.core.template.sql

import kotlin.test.Test
import kotlin.test.assertEquals

class SqlLocationTest {
    @Test
    fun `line=1,column=3,length=5`() {
        val sql = """
            |123456789
            |123456789
            |123456789
        """.trimMargin()
        val location = SqlLocation(sql, 1, 0, 3, 5)
        assertEquals(
            """
            |[
            |12>>>34567<<<89
            |123456789
            |123456789
            |]:1:3
            """.trimMargin(),
            location.toString(),
        )
    }

    @Test
    fun `line=2,column=3,length=5`() {
        val sql = """
            |123456789
            |123456789
            |123456789
        """.trimMargin()
        val location = SqlLocation(sql, 2, 0, 3, 5)
        assertEquals(
            """
            |[
            |123456789
            |12>>>34567<<<89
            |123456789
            |]:2:3
            """.trimMargin(),
            location.toString(),
        )
    }

    @Test
    fun `line=3,column=3,length=5`() {
        val sql = """
            |123456789
            |123456789
            |123456789
        """.trimMargin()
        val location = SqlLocation(sql, 3, 0, 3, 5)
        assertEquals(
            """
            |[
            |123456789
            |123456789
            |12>>>34567<<<89
            |]:3:3
            """.trimMargin(),
            location.toString(),
        )
    }

    @Test
    fun `line=3,column=1,length=5`() {
        val sql = """
            |123456789
            |123456789
            |123456789
        """.trimMargin()
        val location = SqlLocation(sql, 3, 0, 1, 5)
        assertEquals(
            """
            |[
            |123456789
            |123456789
            |>>>12345<<<6789
            |]:3:1
            """.trimMargin(),
            location.toString(),
        )
    }

    @Test
    fun `line=3,column=5,length=5`() {
        val sql = """
            |123456789
            |123456789
            |123456789
        """.trimMargin()
        val location = SqlLocation(sql, 3, 0, 5, 5)
        assertEquals(
            """
            |[
            |123456789
            |123456789
            |1234>>>56789<<<
            |]:3:5
            """.trimMargin(),
            location.toString(),
        )
    }

    @Test
    fun `line=4(illegal),column=5,length=5`() {
        val sql = """
            |123456789
            |123456789
            |123456789
        """.trimMargin()
        val location = SqlLocation(sql, 4, 0, 5, 5)
        assertEquals(
            """
            |[
            |123456789
            |123456789
            |123456789
            |]:4:5
            """.trimMargin(),
            location.toString(),
        )
    }

    @Test
    fun `line=3,column=10(illegal),length=5`() {
        val sql = """
            |123456789
            |123456789
            |123456789
        """.trimMargin()
        val location = SqlLocation(sql, 3, 0, 10, 5)
        assertEquals(
            """
            |[
            |123456789
            |123456789
            |123456789
            |]:3:10
            """.trimMargin(),
            location.toString(),
        )
    }

    @Test
    fun `line=3,column=5,length=6(illegal)`() {
        val sql = """
            |123456789
            |123456789
            |123456789
        """.trimMargin()
        val location = SqlLocation(sql, 3, 0, 5, 6)
        assertEquals(
            """
            |[
            |123456789
            |123456789
            |123456789
            |]:3:5
            """.trimMargin(),
            location.toString(),
        )
    }
}
