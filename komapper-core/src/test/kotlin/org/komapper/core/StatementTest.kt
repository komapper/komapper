package org.komapper.core

import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StatementTest {

    @Test
    fun args() {
        val statement =
            Statement(
                listOf(
                    StatementPart.Text("select * from employee where name = "),
                    StatementPart.Value(Value("aaa", typeOf<String>())),
                    StatementPart.Text(" and age = "),
                    StatementPart.Value(Value(20, typeOf<Int>())),
                ),
            )
        assertEquals(listOf(Value("aaa", typeOf<String>()), Value(20, typeOf<Int>())), statement.args)
    }

    @Test
    fun toSql() {
        val statement =
            Statement(
                listOf(
                    StatementPart.Text("select * from employee where name = "),
                    StatementPart.Value(Value("aaa", typeOf<String>())),
                    StatementPart.Text(" and age = "),
                    StatementPart.Value(Value(20, typeOf<Int>())),
                ),
            )
        assertEquals("select * from employee where name = ? and age = ?", statement.toSql())
        assertEquals(
            "select * from employee where name = aaa and age = 20",
            statement.toSqlWithArgs { first, _, _ -> first.toString() },
        )
    }
}
