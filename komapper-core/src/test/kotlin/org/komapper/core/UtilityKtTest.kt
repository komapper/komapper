package org.komapper.core

import kotlin.test.Test
import kotlin.test.assertEquals

internal class UtilityKtTest {

    @Test
    fun toDryRunResult() {
        val statement =
            Statement(
                listOf(
                    StatementPart.Text("select * from employee where name = "),
                    StatementPart.PlaceHolder(Value("aaa")),
                    StatementPart.Text(" and age = "),
                    StatementPart.PlaceHolder(Value(20)),
                ),
            )
        val result = statement.toDryRunResult(DryRunDialect)
        println(result)
        assertEquals("select * from employee where name = ? and age = ?", result.sql)
        assertEquals("select * from employee where name = 'aaa' and age = 20", result.sqlWithArgs)
        assertEquals(listOf(Value("aaa"), Value(20)), result.args)
    }
}
