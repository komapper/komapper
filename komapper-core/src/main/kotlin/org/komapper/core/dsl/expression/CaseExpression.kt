package org.komapper.core.dsl.expression

import org.komapper.core.dsl.scope.FilterScopeSupport
import org.komapper.core.dsl.scope.WhenScope

internal class CaseExpression<T : Any, S : Any>(
    private val firstWhen: When<T, S>,
    remainingWhen: List<When<T, S>>,
    val otherwise: ColumnExpression<T, S>?,
) :
    ColumnExpression<T, S> by firstWhen.then {
    val whenList: List<When<T, S>> = listOf(firstWhen) + remainingWhen
}

class When<S : Any, T : Any>(val declaration: WhenDeclaration, internal val then: ColumnExpression<S, T>) {
    val criteria: List<Criterion>
        get() {
            val support = FilterScopeSupport()
            WhenScope(support).apply(declaration)
            return support.toList()
        }
    val thenOperand: Operand.Column = Operand.Column(then)
}
