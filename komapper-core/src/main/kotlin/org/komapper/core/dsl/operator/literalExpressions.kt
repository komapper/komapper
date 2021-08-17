package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.BooleanExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.IntExpression
import org.komapper.core.dsl.expression.LiteralExpression
import org.komapper.core.dsl.expression.LongExpression
import org.komapper.core.dsl.expression.StringExpression

fun literal(value: Boolean): ColumnExpression<Boolean, Boolean> {
    return LiteralExpression(value, BooleanExpression)
}

fun literal(value: Int): ColumnExpression<Int, Int> {
    return LiteralExpression(value, IntExpression)
}

fun literal(value: Long): ColumnExpression<Long, Long> {
    return LiteralExpression(value, LongExpression)
}

fun literal(value: String): ColumnExpression<String, String> {
    require("'" !in value) { "The value must not contain the single quotation." }
    return LiteralExpression(value, StringExpression)
}
