package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.query.Subquery

interface FilterScope {
    infix fun <T : Any> ColumnExpression<T>.eq(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.eq(operand: T?)

    infix fun <T : Any> T?.eq(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.notEq(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.notEq(operand: T?)

    infix fun <T : Any> T?.notEq(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.less(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.less(operand: T?)

    infix fun <T : Any> T?.less(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.lessEq(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.lessEq(operand: T?)

    infix fun <T : Any> T?.lessEq(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.greater(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.greater(operand: T?)

    infix fun <T : Any> T?.greater(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.greaterEq(operand: ColumnExpression<T>)

    infix fun <T : Any> ColumnExpression<T>.greaterEq(operand: T?)

    infix fun <T : Any> T?.greaterEq(operand: ColumnExpression<T>)

    fun <T : Any> ColumnExpression<T>.isNull()

    fun <T : Any> ColumnExpression<T>.isNotNull()

    infix fun <T : CharSequence> ColumnExpression<T>.like(operand: CharSequence?)

    infix fun <T : CharSequence> ColumnExpression<T>.notLike(operand: CharSequence?)

    infix fun <T : CharSequence> ColumnExpression<T>.startsWith(operand: CharSequence?)

    infix fun <T : CharSequence> ColumnExpression<T>.notStartsWith(operand: CharSequence?)

    infix fun <T : CharSequence> ColumnExpression<T>.contains(operand: CharSequence?)

    infix fun <T : CharSequence> ColumnExpression<T>.notContains(operand: CharSequence?)

    infix fun <T : CharSequence> ColumnExpression<T>.endsWith(operand: CharSequence?)

    infix fun <T : CharSequence> ColumnExpression<T>.notEndsWith(operand: CharSequence?)

    infix fun <T : Comparable<T>> ColumnExpression<T>.between(range: ClosedRange<T>)

    infix fun <T : Comparable<T>> ColumnExpression<T>.notBetween(range: ClosedRange<T>)

    infix fun <T : Any> ColumnExpression<T>.inList(values: List<T?>)

    infix fun <T : Any> ColumnExpression<T>.inList(block: () -> Subquery<T?>)

    infix fun <T : Any> ColumnExpression<T>.notInList(values: List<T?>)

    infix fun <T : Any> ColumnExpression<T>.notInList(block: () -> Subquery<T?>)

    infix fun <A : Any, B : Any> Pair<ColumnExpression<A>, ColumnExpression<B>>.inList2(values: List<Pair<A?, B?>>)

    infix fun <A : Any, B : Any> Pair<ColumnExpression<A>, ColumnExpression<B>>.inList2(block: () -> Subquery<Pair<A?, B?>>)

    infix fun <A : Any, B : Any> Pair<ColumnExpression<A>, ColumnExpression<B>>.notInList2(values: List<Pair<A?, B?>>)

    infix fun <A : Any, B : Any> Pair<ColumnExpression<A>, ColumnExpression<B>>.notInList2(block: () -> Subquery<Pair<A?, B?>>)

    fun exists(block: () -> Subquery<*>)

    fun notExists(block: () -> Subquery<*>)

    fun text(value: CharSequence): EscapeExpression {
        if (value is EscapeExpression) return value
        return EscapeExpression.Text(value)
    }

    fun escape(value: CharSequence): EscapeExpression {
        if (value is EscapeExpression) return value
        return EscapeExpression.Escape(value)
    }

    fun CharSequence.asPrefix(): CharSequence {
        return escape(this) + text("%")
    }

    fun CharSequence.asInfix(): CharSequence {
        return text("%") + escape(this) + text("%")
    }

    fun CharSequence.asSuffix(): CharSequence {
        return text("%") + escape(this)
    }
}
