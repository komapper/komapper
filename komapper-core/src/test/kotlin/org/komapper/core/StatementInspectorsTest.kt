package org.komapper.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StatementInspectorsTest {

    @Test
    fun decorate() {
        val statement = Statement("hello")
        val inspector1 = StatementInspector { it + "1" }
        val inspector2 = StatementInspector { it + "2" }
        val inspector = listOf(inspector1, inspector2).reduce(StatementInspectors::decorate)
        val result = inspector.inspect(statement)
        println(result.toSql())
        assertEquals("hello12", result.toSql())
    }
}
