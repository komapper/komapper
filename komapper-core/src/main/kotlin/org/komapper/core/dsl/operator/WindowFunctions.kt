package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.CumeDist
import org.komapper.core.dsl.expression.DenseRank
import org.komapper.core.dsl.expression.Lag
import org.komapper.core.dsl.expression.Lead
import org.komapper.core.dsl.expression.Ntile
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.OverDeclaration
import org.komapper.core.dsl.expression.PercentRank
import org.komapper.core.dsl.expression.Rank
import org.komapper.core.dsl.expression.RowNumber
import org.komapper.core.dsl.expression.WindowDefinitionImpl
import org.komapper.core.dsl.expression.WindowFunction
import org.komapper.core.dsl.scope.OverScopeImpl

fun <T : Any, S : Any> WindowFunction<T, S>.over(declaration: OverDeclaration): ColumnExpression<T, S> {
    val scope = OverScopeImpl()
    scope.apply(declaration)
    return WindowDefinitionImpl(this, scope.partitionBy, scope.orderBy, scope.frame)
}

fun rowNumber(): WindowFunction<Long, Long> {
    return RowNumber
}

fun rank(): WindowFunction<Long, Long> {
    return Rank
}

fun denseRank(): WindowFunction<Long, Long> {
    return DenseRank
}

fun percentRank(): WindowFunction<Double, Double> {
    return PercentRank
}

fun cumeDist(): WindowFunction<Double, Double> {
    return CumeDist
}

fun ntile(bucketSize: Int): WindowFunction<Int, Int> {
    val argument = Operand.Argument(literal(bucketSize), bucketSize)
    return Ntile(argument)
}

fun <T : Any, S : Any> lead(
    expression: ColumnExpression<T, S>,
    offset: Int? = null,
    default: ColumnExpression<T, S>? = null
): WindowFunction<T, S> {
    val o1 = offset?.let { Operand.Argument(literal(offset), offset) }
    val o2 = default?.let { Operand.Column(it) }
    return Lead(expression, o1, o2)
}

fun <T : Any, S : Any> lag(
    expression: ColumnExpression<T, S>,
    offset: Int? = null,
    default: ColumnExpression<T, S>? = null
): WindowFunction<T, S> {
    val o1 = offset?.let { Operand.Argument(literal(offset), offset) }
    val o2 = default?.let { Operand.Column(it) }
    return Lag(expression, o1, o2)
}
