package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.MathematicalFunction
import java.math.BigDecimal

/**
 * Builds a RANDOM function.
 */
fun random(): ColumnExpression<BigDecimal, BigDecimal> {
    return MathematicalFunction.Random()
}
