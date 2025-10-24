package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.NullLiteralExpression
import org.komapper.core.dsl.expression.NullableLiteralExpression
import org.komapper.core.dsl.expression.createSimpleNullableLiteralExpression
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.reflect.typeOf

/**
 * Builds a null literal.
 */
inline fun <reified EXTERIOR : Any> nullLiteral(): ColumnExpression<EXTERIOR, *> {
    return NullLiteralExpression<EXTERIOR, String>(typeOf<EXTERIOR>(), typeOf<String>())
}

/**
 * Builds a null literal.
 */
fun <EXTERIOR : Any, INTERIOR : Any> nullLiteral(expression: ColumnExpression<EXTERIOR, INTERIOR>): ColumnExpression<EXTERIOR, INTERIOR> {
    return NullLiteralExpression(expression.exteriorType, expression.interiorType)
}

/**
 * Builds a [Boolean] literal.
 */
fun literal(value: Boolean?): ColumnExpression<Boolean, Boolean> {
    return createSimpleNullableLiteralExpression(value, typeOf<Boolean>())
}

/**
 * Builds a [Int] literal.
 */
fun literal(value: Int?): ColumnExpression<Int, Int> {
    return createSimpleNullableLiteralExpression(value, typeOf<Int>())
}

/**
 * Builds a [Long] literal.
 */
fun literal(value: Long?): ColumnExpression<Long, Long> {
    return createSimpleNullableLiteralExpression(value, typeOf<Long>())
}

/**
 * Builds a [BigDecimal] literal.
 */
fun literal(value: BigDecimal?): ColumnExpression<BigDecimal, BigDecimal> {
    return createSimpleNullableLiteralExpression(value, typeOf<BigDecimal>())
}

/**
 * Builds a [Double] literal.
 */
fun literal(value: Double?): ColumnExpression<Double, Double> {
    return createSimpleNullableLiteralExpression(value, typeOf<Double>())
}

/**
 * Builds a [OffsetDateTime] literal.
 */
fun literal(value: OffsetDateTime?): ColumnExpression<OffsetDateTime, OffsetDateTime> {
    return createSimpleNullableLiteralExpression(value, typeOf<OffsetDateTime>())
}

/**
 * Builds a [String] literal.
 * @param value the value of the literal.
 * @exception IllegalArgumentException if the value contains the single quotation.
 */
fun literal(value: String?): ColumnExpression<String, String> {
    if (value != null && containsSingleQuote(value)) {
        throw IllegalArgumentException("The value must not contain the single quote.")
    }
    return createSimpleNullableLiteralExpression(value, typeOf<String>())
}

internal fun containsSingleQuote(value: String): Boolean {
    var quote = false
    for (c in value) {
        if (c == '\'') {
            quote = !quote
        } else if (quote) {
            return true
        }
    }
    return quote
}

/**
 * Creates a column expression representing a literal value. This function allows you to handle
 * Enum types or user-defined types.
 *
 * @param value the value of the literal
 * @param expression the column expression defining the type mappings and conversion functions
 * @return a column expression that represents the literal value
 */
fun <EXTERNAL : Any, INTERNAL : Any> literal(
    value: EXTERNAL?,
    expression: ColumnExpression<EXTERNAL, INTERNAL>,
): ColumnExpression<EXTERNAL, INTERNAL> {
    val internalValue = if (value == null) null else expression.unwrap(value)
    return NullableLiteralExpression(
        internalValue,
        exteriorType = expression.exteriorType,
        interiorType = expression.interiorType,
        wrap = expression.wrap,
        unwrap = expression.unwrap
    )
}
