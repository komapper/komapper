package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.OverDeclaration
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
