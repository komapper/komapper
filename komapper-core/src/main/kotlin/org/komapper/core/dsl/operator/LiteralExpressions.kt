package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.NonNullLiteralExpression
import org.komapper.core.dsl.expression.NullLiteralExpression
import java.math.BigDecimal
import kotlin.reflect.KClass

/**
 * Builds a null literal.
 */
inline fun <reified EXTERIOR : Any> nullLiteral(): ColumnExpression<EXTERIOR, *> {
    return NullLiteralExpression(EXTERIOR::class, String::class)
}

/**
 * Builds a null literal.
 */
fun <T : Any> nullLiteral(klass: KClass<T>): ColumnExpression<T, T> {
    return NullLiteralExpression(klass, klass)
}

/**
 * Builds a null literal.
 */
fun <EXTERIOR : Any, INTERIOR : Any> nullLiteral(exteriorClass: KClass<EXTERIOR>, interiorClass: KClass<INTERIOR>): ColumnExpression<EXTERIOR, INTERIOR> {
    return NullLiteralExpression(exteriorClass, interiorClass)
}

/**
 * Builds a [Boolean] literal.
 */
fun literal(value: Boolean): ColumnExpression<Boolean, Boolean> {
    return NonNullLiteralExpression(value, Boolean::class)
}

/**
 * Builds a [Int] literal.
 */
fun literal(value: Int): ColumnExpression<Int, Int> {
    return NonNullLiteralExpression(value, Int::class)
}

/**
 * Builds a [Long] literal.
 */
fun literal(value: Long): ColumnExpression<Long, Long> {
    return NonNullLiteralExpression(value, Long::class)
}

/**
 * Builds a [BigDecimal] literal.
 */
fun literal(value: BigDecimal): ColumnExpression<BigDecimal, BigDecimal> {
    return NonNullLiteralExpression(value, BigDecimal::class)
}

/**
 * Builds a [String] literal.
 * @param value the value of the literal.
 * @exception IllegalArgumentException if the value contains the single quotation.
 */
fun literal(value: String): ColumnExpression<String, String> {
    require("'" !in value) { "The value must not contain the single quotation." }
    return NonNullLiteralExpression(value, String::class)
}
