package org.komapper.sqlcommenter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.Statement
import org.komapper.core.StatementPart

internal class SqlCommenterTest {

    @Test
    fun inspect() {
        val inspector = SqlCommenter()
        val statement = Statement(listOf(StatementPart.Text("select * from employee")))
        val newStatement = inspector.inspect(statement)
        assertEquals(2, newStatement.parts.size)
    }
}
