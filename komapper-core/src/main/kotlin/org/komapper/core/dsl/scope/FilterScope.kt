package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.SubqueryExpression

interface FilterScope {
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.eq(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.eq(operand: T?)

    infix fun <T : Any, S : Any> T?.eq(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.notEq(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.notEq(operand: T?)

    infix fun <T : Any, S : Any> T?.notEq(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.less(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.less(operand: T?)

    infix fun <T : Any, S : Any> T?.less(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.lessEq(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.lessEq(operand: T?)

    infix fun <T : Any, S : Any> T?.lessEq(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.greater(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.greater(operand: T?)

    infix fun <T : Any, S : Any> T?.greater(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.greaterEq(operand: ColumnExpression<T, S>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.greaterEq(operand: T?)

    infix fun <T : Any, S : Any> T?.greaterEq(operand: ColumnExpression<T, S>)

    fun <T : Any, S : Any> ColumnExpression<T, S>.isNull()

    fun <T : Any, S : Any> ColumnExpression<T, S>.isNotNull()

    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.like(operand: CharSequence?)

    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notLike(operand: CharSequence?)

    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.startsWith(operand: CharSequence?)

    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notStartsWith(operand: CharSequence?)

    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.contains(operand: CharSequence?)

    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notContains(operand: CharSequence?)

    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.endsWith(operand: CharSequence?)

    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notEndsWith(operand: CharSequence?)

    infix fun <T : Comparable<T>, S : Any> ColumnExpression<T, S>.between(range: ClosedRange<T>)

    infix fun <T : Comparable<T>, S : Any> ColumnExpression<T, S>.notBetween(range: ClosedRange<T>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.inList(values: List<T?>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.inList(block: () -> SubqueryExpression<T?>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.notInList(values: List<T?>)

    infix fun <T : Any, S : Any> ColumnExpression<T, S>.notInList(block: () -> SubqueryExpression<T?>)

    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.inList2(values: List<Pair<A?, B?>>)

    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.inList2(block: () -> SubqueryExpression<Pair<A?, B?>>)

    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.notInList2(values: List<Pair<A?, B?>>)

    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.notInList2(block: () -> SubqueryExpression<Pair<A?, B?>>)

    fun exists(block: () -> SubqueryExpression<*>)

    fun notExists(block: () -> SubqueryExpression<*>)

    fun <S : CharSequence> text(value: S): EscapeExpression {
        if (value is EscapeExpression) return value
        return EscapeExpression.Text(value)
    }

    fun <S : CharSequence> escape(value: S): EscapeExpression {
        if (value is EscapeExpression) return value
        return EscapeExpression.Escape(value)
    }

    fun CharSequence.asPrefix(): EscapeExpression {
        return escape(this) + text("%")
    }

    fun CharSequence.asInfix(): EscapeExpression {
        return text("%") + escape(this) + text("%")
    }

    fun CharSequence.asSuffix(): EscapeExpression {
        return text("%") + escape(this)
    }
}
