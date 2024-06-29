package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.NonNullLiteralExpression
import org.komapper.core.dsl.expression.NullLiteralExpression
import java.math.BigDecimal
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Builds a null literal.
 */
inline fun <reified T : Any> nullLiteral(): ColumnExpression<T, T> {
    return NullLiteralExpression(typeOf<T>(), typeOf<T>())
}

/**
 * Builds a null literal.
 */
fun <EXTERIOR : Any> nullLiteral(exteriorType: KType): ColumnExpression<EXTERIOR, *> {
    return NullLiteralExpression<EXTERIOR, String>(exteriorType, typeOf<String>())
}

/**
 * Builds a null literal.
 */
fun <EXTERIOR : Any, INTERIOR : Any> nullLiteral(exteriorType: KType, interiorType: KType): ColumnExpression<EXTERIOR, INTERIOR> {
    return NullLiteralExpression(exteriorType, interiorType)
}

/**
 * Builds a [Boolean] literal.
 */
fun literal(value: Boolean): ColumnExpression<Boolean, Boolean> {
    return NonNullLiteralExpression(value, typeOf<Boolean>())
}

/**
 * Builds a [Int] literal.
 */
fun literal(value: Int): ColumnExpression<Int, Int> {
    return NonNullLiteralExpression(value, typeOf<Int>())
}

/**
 * Builds a [Long] literal.
 */
fun literal(value: Long): ColumnExpression<Long, Long> {
    return NonNullLiteralExpression(value, typeOf<Long>())
}

/**
 * Builds a [BigDecimal] literal.
 */
fun literal(value: BigDecimal): ColumnExpression<BigDecimal, BigDecimal> {
    return NonNullLiteralExpression(value, typeOf<BigDecimal>())
}

/**
 * Builds a [String] literal.
 * @param value the value of the literal.
 * @exception IllegalArgumentException if the value contains the single quotation.
 */
fun literal(value: String): ColumnExpression<String, String> {
    require("'" !in value) { "The value must not contain the single quotation." }
    return NonNullLiteralExpression(value, typeOf<String>())
}
