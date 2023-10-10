package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.CompositeColumnExpression
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.operator.CriteriaContext

/**
 * Provides operators and predicates for HAVING, ON, WHEN, and WHERE clauses.
 */
interface FilterScope<F : FilterScope<F>> {
    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.eq(operand: ColumnExpression<T, S>)

    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.eq(operand: T?)

    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any, S : Any> T?.eq(operand: ColumnExpression<T, S>)

    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any> CompositeColumnExpression<T>.eq(operand: T?)

    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any> T?.eq(operand: CompositeColumnExpression<T>)

    /**
     * Applies the `<>` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.notEq(operand: ColumnExpression<T, S>)

    /**
     * Applies the `<>` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.notEq(operand: T?)

    /**
     * Applies the `<>` operator.
     */
    infix fun <T : Any, S : Any> T?.notEq(operand: ColumnExpression<T, S>)

    /**
     * Applies the `<` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.less(operand: ColumnExpression<T, S>)

    /**
     * Applies the `<` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.less(operand: T?)

    /**
     * Applies the `<` operator.
     */
    infix fun <T : Any, S : Any> T?.less(operand: ColumnExpression<T, S>)

    /**
     * Applies the `<=` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.lessEq(operand: ColumnExpression<T, S>)

    /**
     * Applies the `<=` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.lessEq(operand: T?)

    /**
     * Applies the `<=` operator.
     */
    infix fun <T : Any, S : Any> T?.lessEq(operand: ColumnExpression<T, S>)

    /**
     * Applies the `>` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.greater(operand: ColumnExpression<T, S>)

    /**
     * Applies the `>` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.greater(operand: T?)

    /**
     * Applies the `>` operator.
     */
    infix fun <T : Any, S : Any> T?.greater(operand: ColumnExpression<T, S>)

    /**
     * Applies the `>=` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.greaterEq(operand: ColumnExpression<T, S>)

    /**
     * Applies the `>=` operator.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.greaterEq(operand: T?)

    /**
     * Applies the `>=` operator.
     */
    infix fun <T : Any, S : Any> T?.greaterEq(operand: ColumnExpression<T, S>)

    /**
     * Applies the `IS NULL` predicate.
     */
    fun <T : Any, S : Any> ColumnExpression<T, S>.isNull()

    /**
     * Applies the `IS NOT NULL` predicate.
     */
    fun <T : Any, S : Any> ColumnExpression<T, S>.isNotNull()

    /**
     * Applies the `LIKE` predicate.
     */
    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.like(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     */
    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notLike(operand: CharSequence?)

    /**
     * Applies the `LIKE` predicate.
     * It is translated to `LIKE operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.startsWith(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     * It is translated to `NOT LIKE operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notStartsWith(operand: CharSequence?)

    /**
     * Applies the `LIKE` predicate.
     * It is translated to `LIKE '%' + operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.contains(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     * It is translated to `NOT LIKE '%' + operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notContains(operand: CharSequence?)

    /**
     * Applies the `LIKE` predicate.
     * It is translated to `LIKE '%' + operand`.
     */
    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.endsWith(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     * It is translated to `NOT LIKE '%' + operand`.
     */
    infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notEndsWith(operand: CharSequence?)

    /**
     * Applies the `BETWEEN` predicate.
     */
    infix fun <T : Comparable<T>, S : Any> ColumnExpression<T, S>.between(range: ClosedRange<T>)

    /**
     * Applies the `NOT BETWEEN` predicate.
     */
    infix fun <T : Comparable<T>, S : Any> ColumnExpression<T, S>.notBetween(range: ClosedRange<T>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.inList(values: List<T?>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.inList(subquery: SubqueryExpression<T?>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.inList(block: () -> SubqueryExpression<T?>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.notInList(values: List<T?>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.notInList(subquery: SubqueryExpression<T?>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <T : Any, S : Any> ColumnExpression<T, S>.notInList(block: () -> SubqueryExpression<T?>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.inList2(values: List<Pair<A?, B?>>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.inList2(subquery: SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.inList2(block: () -> SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.notInList2(values: List<Pair<A?, B?>>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.notInList2(subquery: SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.notInList2(block: () -> SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `EXISTS` predicate.
     */
    fun exists(subquery: SubqueryExpression<*>)

    /**
     * Applies the `EXISTS` predicate.
     */
    fun exists(block: () -> SubqueryExpression<*>)

    /**
     * Applies the `NOT EXISTS` predicate.
     */
    fun notExists(subquery: SubqueryExpression<*>)

    /**
     * Applies the `NOT EXISTS` predicate.
     */
    fun notExists(block: () -> SubqueryExpression<*>)

    /**
     * Applies the `AND` operator.
     */
    fun and(declaration: F.() -> Unit)

    /**
     * Applies the `OR` operator.
     */
    fun or(declaration: F.() -> Unit)

    /**
     * Applies the `NOT` operator.
     */
    fun not(declaration: F.() -> Unit)

    /**
     * Does not escape the given string.
     */
    fun <S : CharSequence> text(value: S): EscapeExpression {
        if (value is EscapeExpression) return value
        return EscapeExpression.Text(value.toString())
    }

    /**
     * Escapes the given string.
     */
    fun <S : CharSequence> escape(value: S): EscapeExpression {
        if (value is EscapeExpression) return value
        return EscapeExpression.Escape(value.toString())
    }

    /**
     * Escapes the given string and appends a wildcard character at the end.
     */
    fun CharSequence.asPrefix(): EscapeExpression {
        return escape(this) + text("%")
    }

    /**
     * Escapes the given string and encloses it with wildcard characters.
     */
    fun CharSequence.asInfix(): EscapeExpression {
        return text("%") + escape(this) + text("%")
    }

    /**
     * Escapes the given string and appends a wildcard character at the beginning.
     */
    fun CharSequence.asSuffix(): EscapeExpression {
        return text("%") + escape(this)
    }

    /**
     * Adds an extension.
     *
     * @param EXTENSION the type of extension
     * @param construct the extension constructor
     * @param declaration the filter declaration
     */
    fun <EXTENSION> extension(construct: (context: CriteriaContext) -> EXTENSION, declaration: EXTENSION.() -> Unit)
}
