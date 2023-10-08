package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import kotlin.test.Test
import kotlin.test.assertEquals

class OperatorUtilityTest {

    @Test
    fun columnExpression() {
        val value = "world"
        val left = myConcat(literal("hello"), value)
        val right = myConcat(literal("hello"), value)
        assertEquals(left, right)
    }

    private fun myConcat(
        left: ColumnExpression<String, String>,
        @Suppress("SameParameterValue") right: String,
    ): ColumnExpression<String, String> {
        val o1 = Operand.Column(left)
        val o2 = Operand.Argument(left, right)
        return columnExpression(String::class, listOf(o1, o2)) {
            append("concat(")
            visit(o1)
            append(", ")
            visit(o2)
            append(")")
        }
    }
}
