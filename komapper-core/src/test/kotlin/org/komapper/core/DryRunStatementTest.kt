package org.komapper.core

import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

class DryRunStatementTest {
    @Test
    fun plus() {
        val a =
            DryRunStatement(
                sql = "select * from employee where name = ? and age = ?",
                sqlWithArgs = "select * from employee where name = 'aaa' and age = 20",
                args = listOf(Value("aaa", typeOf<String>()), Value(20, typeOf<Int>())),
            )
        val b =
            DryRunStatement(
                sql = "delete from employee where name = ?",
                sqlWithArgs = "delete from employee where name = 'bbb'",
                args = listOf(Value("bbb", typeOf<String>())),
            )
        val c = a + b
        assertEquals("select * from employee where name = ? and age = ?;delete from employee where name = ?", c.sql)
        assertEquals("select * from employee where name = 'aaa' and age = 20;delete from employee where name = 'bbb'", c.sqlWithArgs)
        assertEquals(c.args, listOf(Value("aaa", typeOf<String>()), Value(20, typeOf<Int>()), Value("bbb", typeOf<String>())))
    }
}
