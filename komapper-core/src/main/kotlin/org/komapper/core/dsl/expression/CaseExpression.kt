package org.komapper.core.dsl.expression

import org.komapper.core.dsl.declaration.WhenDeclaration
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.scope.WhenScope

internal class CaseExpression<T : Any, S : Any>(
    private val firstWhen: When<T, S>,
    remainingWhen: List<When<T, S>>,
    val otherwise: ColumnExpression<T, S>?
) :
    ColumnExpression<T, S> by firstWhen.then {
    val whenList: List<When<T, S>> = listOf(firstWhen) + remainingWhen
}

class When<S : Any, T : Any>(declaration: WhenDeclaration, internal val then: ColumnExpression<S, T>) {
    val criteria: List<Criterion> = WhenScope().apply(declaration).toList()
    val thenOperand: Operand.Column = Operand.Column(then)
}
