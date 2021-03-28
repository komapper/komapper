package org.komapper.core.dsl.scope

import org.komapper.core.dsl.operand.LikeOperand
import org.komapper.core.dsl.query.SingleColumnSqlSubqueryResult
import org.komapper.core.dsl.query.SqlSubqueryResult
import org.komapper.core.metamodel.ColumnInfo

interface FilterScope {
    infix fun <T : Any> ColumnInfo<T>.eq(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.eq(operand: T?)

    infix fun <T : Any> T?.eq(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.notEq(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.notEq(operand: T?)

    infix fun <T : Any> T?.notEq(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.less(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.less(operand: T?)

    infix fun <T : Any> T?.less(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.lessEq(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.lessEq(operand: T?)

    infix fun <T : Any> T?.lessEq(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.greater(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.greater(operand: T?)

    infix fun <T : Any> T?.greater(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.greaterEq(operand: ColumnInfo<T>)

    infix fun <T : Any> ColumnInfo<T>.greaterEq(operand: T?)

    infix fun <T : Any> T?.greaterEq(operand: ColumnInfo<T>)
    fun <T : Any> ColumnInfo<T>.isNull()
    fun <T : Any> ColumnInfo<T>.isNotNull()

    infix fun <T : CharSequence> ColumnInfo<T>.like(operand: Any?)

    infix fun <T : CharSequence> ColumnInfo<T>.like(operand: LikeOperand)

    infix fun <T : CharSequence> ColumnInfo<T>.notLike(operand: Any?)

    infix fun <T : CharSequence> ColumnInfo<T>.notLike(operand: LikeOperand)

    infix fun <T : Comparable<T>> ColumnInfo<T>.between(range: ClosedRange<T>)

    infix fun <T : Comparable<T>> ColumnInfo<T>.notBetween(range: ClosedRange<T>)

    infix fun <T : Any> ColumnInfo<T>.inList(values: List<T?>)

    infix fun <T : Any> ColumnInfo<T>.inList(block: () -> SingleColumnSqlSubqueryResult)

    infix fun <T : Any> ColumnInfo<T>.inList(projection: SingleColumnSqlSubqueryResult)

    infix fun <T : Any> ColumnInfo<T>.notInList(values: List<T?>)

    infix fun <T : Any> ColumnInfo<T>.notInList(block: () -> SingleColumnSqlSubqueryResult)

    infix fun <T : Any> ColumnInfo<T>.notInList(projection: SingleColumnSqlSubqueryResult)
    fun exists(block: () -> SqlSubqueryResult)
    fun exists(result: SqlSubqueryResult)
    fun notExists(block: () -> SqlSubqueryResult)
    fun notExists(result: SqlSubqueryResult)
    fun <T : CharSequence> T?.escape(): LikeOperand
    fun <T : CharSequence> T?.asPrefix(): LikeOperand
    fun <T : CharSequence> T?.asInfix(): LikeOperand
    fun <T : CharSequence> T?.asSuffix(): LikeOperand
}
