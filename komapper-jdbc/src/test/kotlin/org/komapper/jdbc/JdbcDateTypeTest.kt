package org.komapper.jdbc

import kotlin.test.Test
import kotlin.test.assertEquals

class JdbcDateTypeTest {
    @Test
    fun localDateLiteral() {
        val dataType = JdbcLocalDateType("date")
        assertEquals("{d '1234-01-02'}", dataType.toLiteral(java.time.LocalDate.of(1234, 1, 2)))
        assertEquals("{d '2020-12-25'}", dataType.toLiteral(java.time.LocalDate.of(2020, 12, 25)))
        assertEquals("null", dataType.toLiteral(null))
    }

    @Test
    fun localDateTimeLiteral() {
        val dataType = JdbcLocalDateTimeType("timestamp")
        assertEquals("{ts '1234-01-02 03:04:05'}", dataType.toLiteral(java.time.LocalDateTime.of(1234, 1, 2, 3, 4, 5)))
        assertEquals("{ts '2020-12-25 13:34:56'}", dataType.toLiteral(java.time.LocalDateTime.of(2020, 12, 25, 13, 34, 56)))
        assertEquals("null", dataType.toLiteral(null))
    }

    @Test
    fun localTimeLiteral() {
        val dataType = JdbcLocalTimeType("time")
        assertEquals("{t '03:04:05'}", dataType.toLiteral(java.time.LocalTime.of(3, 4, 5)))
        assertEquals("{t '13:34:56'}", dataType.toLiteral(java.time.LocalTime.of(13, 34, 56)))
        assertEquals("null", dataType.toLiteral(null))
    }
}
