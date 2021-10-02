package org.komapper.sqlcommenter

import org.komapper.core.Statement
import org.komapper.core.StatementPart
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SqlCommenterTest {

    @Test
    fun inspect() {
        val inspector = SqlCommenter()
        val statement = Statement(listOf(StatementPart.Text("select * from employee")))
        val newStatement = inspector.inspect(statement)
        assertEquals(2, newStatement.parts.size)
    }
}
