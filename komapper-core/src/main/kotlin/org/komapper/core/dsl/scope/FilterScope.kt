package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expr.PropertyExpression
import org.komapper.core.dsl.operand.LikeOperand
import org.komapper.core.dsl.query.SingleColumnSqlSubqueryResult
import org.komapper.core.dsl.query.SqlSubqueryResult

interface FilterScope {
    infix fun <T : Any> PropertyExpression<T>.eq(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.eq(operand: T?)

    infix fun <T : Any> T?.eq(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.notEq(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.notEq(operand: T?)

    infix fun <T : Any> T?.notEq(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.less(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.less(operand: T?)

    infix fun <T : Any> T?.less(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.lessEq(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.lessEq(operand: T?)

    infix fun <T : Any> T?.lessEq(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.greater(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.greater(operand: T?)

    infix fun <T : Any> T?.greater(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.greaterEq(operand: PropertyExpression<T>)

    infix fun <T : Any> PropertyExpression<T>.greaterEq(operand: T?)

    infix fun <T : Any> T?.greaterEq(operand: PropertyExpression<T>)
    fun <T : Any> PropertyExpression<T>.isNull()
    fun <T : Any> PropertyExpression<T>.isNotNull()

    infix fun <T : CharSequence> PropertyExpression<T>.like(operand: Any?)

    infix fun <T : CharSequence> PropertyExpression<T>.like(operand: LikeOperand)

    infix fun <T : CharSequence> PropertyExpression<T>.notLike(operand: Any?)

    infix fun <T : CharSequence> PropertyExpression<T>.notLike(operand: LikeOperand)

    infix fun <T : Comparable<T>> PropertyExpression<T>.between(range: ClosedRange<T>)

    infix fun <T : Comparable<T>> PropertyExpression<T>.notBetween(range: ClosedRange<T>)

    infix fun <T : Any> PropertyExpression<T>.inList(values: List<T?>)

    infix fun <T : Any> PropertyExpression<T>.inList(block: () -> SingleColumnSqlSubqueryResult)

    infix fun <T : Any> PropertyExpression<T>.inList(projection: SingleColumnSqlSubqueryResult)

    infix fun <T : Any> PropertyExpression<T>.notInList(values: List<T?>)

    infix fun <T : Any> PropertyExpression<T>.notInList(block: () -> SingleColumnSqlSubqueryResult)

    infix fun <T : Any> PropertyExpression<T>.notInList(projection: SingleColumnSqlSubqueryResult)
    fun exists(block: () -> SqlSubqueryResult)
    fun exists(result: SqlSubqueryResult)
    fun notExists(block: () -> SqlSubqueryResult)
    fun notExists(result: SqlSubqueryResult)
    fun <T : CharSequence> T?.escape(): LikeOperand
    fun <T : CharSequence> T?.asPrefix(): LikeOperand
    fun <T : CharSequence> T?.asInfix(): LikeOperand
    fun <T : CharSequence> T?.asSuffix(): LikeOperand
}
