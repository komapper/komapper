package org.komapper.core.dsl.scope

import org.komapper.core.dsl.operand.LikeOperand
import org.komapper.core.dsl.query.SingleColumnSqlSubqueryResult
import org.komapper.core.dsl.query.SqlSubqueryResult
import org.komapper.core.metamodel.Column

interface FilterScope {
    infix fun <T : Any> Column<T>.eq(operand: Column<T>)

    infix fun <T : Any> Column<T>.eq(operand: T?)

    infix fun <T : Any> T?.eq(operand: Column<T>)

    infix fun <T : Any> Column<T>.notEq(operand: Column<T>)

    infix fun <T : Any> Column<T>.notEq(operand: T?)

    infix fun <T : Any> T?.notEq(operand: Column<T>)

    infix fun <T : Any> Column<T>.less(operand: Column<T>)

    infix fun <T : Any> Column<T>.less(operand: T?)

    infix fun <T : Any> T?.less(operand: Column<T>)

    infix fun <T : Any> Column<T>.lessEq(operand: Column<T>)

    infix fun <T : Any> Column<T>.lessEq(operand: T?)

    infix fun <T : Any> T?.lessEq(operand: Column<T>)

    infix fun <T : Any> Column<T>.greater(operand: Column<T>)

    infix fun <T : Any> Column<T>.greater(operand: T?)

    infix fun <T : Any> T?.greater(operand: Column<T>)

    infix fun <T : Any> Column<T>.greaterEq(operand: Column<T>)

    infix fun <T : Any> Column<T>.greaterEq(operand: T?)

    infix fun <T : Any> T?.greaterEq(operand: Column<T>)
    fun <T : Any> Column<T>.isNull()
    fun <T : Any> Column<T>.isNotNull()

    infix fun <T : CharSequence> Column<T>.like(operand: Any?)

    infix fun <T : CharSequence> Column<T>.like(operand: LikeOperand)

    infix fun <T : CharSequence> Column<T>.notLike(operand: Any?)

    infix fun <T : CharSequence> Column<T>.notLike(operand: LikeOperand)

    infix fun <T : Comparable<T>> Column<T>.between(range: ClosedRange<T>)

    infix fun <T : Comparable<T>> Column<T>.notBetween(range: ClosedRange<T>)

    infix fun <T : Any> Column<T>.inList(values: List<T?>)

    infix fun <T : Any> Column<T>.inList(block: () -> SingleColumnSqlSubqueryResult)

    infix fun <T : Any> Column<T>.inList(projection: SingleColumnSqlSubqueryResult)

    infix fun <T : Any> Column<T>.notInList(values: List<T?>)

    infix fun <T : Any> Column<T>.notInList(block: () -> SingleColumnSqlSubqueryResult)

    infix fun <T : Any> Column<T>.notInList(projection: SingleColumnSqlSubqueryResult)
    fun exists(block: () -> SqlSubqueryResult)
    fun exists(result: SqlSubqueryResult)
    fun notExists(block: () -> SqlSubqueryResult)
    fun notExists(result: SqlSubqueryResult)
    fun <T : CharSequence> T?.escape(): LikeOperand
    fun <T : CharSequence> T?.asPrefix(): LikeOperand
    fun <T : CharSequence> T?.asInfix(): LikeOperand
    fun <T : CharSequence> T?.asSuffix(): LikeOperand
}
