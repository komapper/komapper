package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.LiteralExpression
import java.math.BigDecimal

/**
 * Builds a [Boolean] literal.
 */
fun literal(value: Boolean): ColumnExpression<Boolean, Boolean> {
    return LiteralExpression(value, Boolean::class)
}

/**
 * Builds a [Int] literal.
 */
fun literal(value: Int): ColumnExpression<Int, Int> {
    return LiteralExpression(value, Int::class)
}

/**
 * Builds a [Long] literal.
 */
fun literal(value: Long): ColumnExpression<Long, Long> {
    return LiteralExpression(value, Long::class)
}

/**
 * Builds a [BigDecimal] literal.
 */
fun literal(value: BigDecimal): ColumnExpression<BigDecimal, BigDecimal> {
    return LiteralExpression(value, BigDecimal::class)
}

/**
 * Builds a [String] literal.
 * @param value the value of the literal.
 * @exception IllegalArgumentException if the value contains the single quotation.
 */
fun literal(value: String): ColumnExpression<String, String> {
    require("'" !in value) { "The value must not contain the single quotation." }
    return LiteralExpression(value, String::class)
}
