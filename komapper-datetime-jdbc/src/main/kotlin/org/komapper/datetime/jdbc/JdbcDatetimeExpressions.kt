package org.komapper.datetime.jdbc

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.createSimpleNullableLiteralExpression
import kotlin.reflect.typeOf

/**
 * Builds a [LocalDate] literal.
 */
fun literal(value: LocalDate?): ColumnExpression<LocalDate, LocalDate> {
    return createSimpleNullableLiteralExpression(value, typeOf<LocalDate>())
}

/**
 * Builds a [LocalDateTime] literal.
 */
fun literal(value: LocalDateTime?): ColumnExpression<LocalDateTime, LocalDateTime> {
    return createSimpleNullableLiteralExpression(value, typeOf<LocalDateTime>())
}
