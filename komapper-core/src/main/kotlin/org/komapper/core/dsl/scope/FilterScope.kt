package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.query.Subquery

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

    infix fun <T : CharSequence> PropertyExpression<T>.like(operand: CharSequence?)

    infix fun <T : CharSequence> PropertyExpression<T>.notLike(operand: CharSequence?)

    infix fun <T : CharSequence> PropertyExpression<T>.startsWith(operand: CharSequence?)

    infix fun <T : CharSequence> PropertyExpression<T>.notStartsWith(operand: CharSequence?)

    infix fun <T : CharSequence> PropertyExpression<T>.contains(operand: CharSequence?)

    infix fun <T : CharSequence> PropertyExpression<T>.notContains(operand: CharSequence?)

    infix fun <T : CharSequence> PropertyExpression<T>.endsWith(operand: CharSequence?)

    infix fun <T : CharSequence> PropertyExpression<T>.notEndsWith(operand: CharSequence?)

    infix fun <T : Comparable<T>> PropertyExpression<T>.between(range: ClosedRange<T>)

    infix fun <T : Comparable<T>> PropertyExpression<T>.notBetween(range: ClosedRange<T>)

    infix fun <T : Any> PropertyExpression<T>.inList(values: List<T?>)

    infix fun <T : Any> PropertyExpression<T>.inList(block: () -> Subquery<T?>)

    infix fun <T : Any> PropertyExpression<T>.notInList(values: List<T?>)

    infix fun <T : Any> PropertyExpression<T>.notInList(block: () -> Subquery<T?>)

    infix fun <A : Any, B : Any> Pair<PropertyExpression<A>, PropertyExpression<B>>.inList2(values: List<Pair<A?, B?>>)

    infix fun <A : Any, B : Any> Pair<PropertyExpression<A>, PropertyExpression<B>>.inList2(block: () -> Subquery<Pair<A?, B?>>)

    infix fun <A : Any, B : Any> Pair<PropertyExpression<A>, PropertyExpression<B>>.notInList2(values: List<Pair<A?, B?>>)

    infix fun <A : Any, B : Any> Pair<PropertyExpression<A>, PropertyExpression<B>>.notInList2(block: () -> Subquery<Pair<A?, B?>>)

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
