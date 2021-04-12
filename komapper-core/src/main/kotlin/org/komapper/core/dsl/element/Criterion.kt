package org.komapper.core.dsl.element

import org.komapper.core.dsl.context.SubqueryContext

sealed class Criterion {
    data class Eq(val left: Operand, val right: Operand) : Criterion()
    data class NotEq(val left: Operand, val right: Operand) : Criterion()
    data class Less(val left: Operand, val right: Operand) : Criterion()
    data class LessEq(val left: Operand, val right: Operand) : Criterion()
    data class Grater(val left: Operand, val right: Operand) : Criterion()
    data class GraterEq(val left: Operand, val right: Operand) : Criterion()
    data class IsNull(val left: Operand) : Criterion()
    data class IsNotNull(val left: Operand) : Criterion()
    data class Like(val left: Operand, val right: Operand) : Criterion()
    data class NotLike(val left: Operand, val right: Operand) : Criterion()
    data class Between(val left: Operand, val right: Pair<Operand, Operand>) : Criterion()
    data class NotBetween(val left: Operand, val right: Pair<Operand, Operand>) : Criterion()
    data class InList(val left: Operand, val right: List<Operand>) : Criterion()
    data class NotInList(val left: Operand, val right: List<Operand>) : Criterion()
    data class InSubQuery(val left: Operand, val right: SubqueryContext<*>) : Criterion()
    data class NotInSubQuery(val left: Operand, val right: SubqueryContext<*>) : Criterion()
    data class InList2(val left: Pair<Operand, Operand>, val right: List<Pair<Operand, Operand>>) : Criterion()
    data class NotInList2(val left: Pair<Operand, Operand>, val right: List<Pair<Operand, Operand>>) : Criterion()
    data class InSubQuery2(val left: Pair<Operand, Operand>, val right: SubqueryContext<*>) : Criterion()
    data class NotInSubQuery2(val left: Pair<Operand, Operand>, val right: SubqueryContext<*>) : Criterion()
    data class Exists(val context: SubqueryContext<*>) : Criterion()
    data class NotExists(val context: SubqueryContext<*>) : Criterion()

    data class And(val criteria: List<Criterion>) : Criterion()
    data class Or(val criteria: List<Criterion>) : Criterion()
    data class Not(val criteria: List<Criterion>) : Criterion()
}
