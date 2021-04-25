package org.komapper.core.dsl.scope

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.query.Subquery

internal class FilterScopeSupport(
    private val context: MutableList<Criterion> = mutableListOf()
) : FilterScope, List<Criterion> by context {

    internal fun add(criterion: Criterion) {
        context.add(criterion)
    }

    private fun <T : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: ColumnExpression<T>,
        right: ColumnExpression<T>
    ) {
        context.add(operator(Operand.Column(left), Operand.Column(right)))
    }

    private fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: ColumnExpression<T>, right: T) {
        context.add(operator(Operand.Column(left), Operand.Argument(left.klass, right)))
    }

    private fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: T, right: ColumnExpression<T>) {
        context.add(operator(Operand.Argument(right.klass, left), Operand.Column(right)))
    }

    private fun <T : CharSequence> addLikeOperator(left: ColumnExpression<T>, right: CharSequence) {
        context.add(Criterion.Like(Operand.Column(left), Operand.Argument(left.klass, right)))
    }

    private fun <T : CharSequence> addNotLikeOperator(left: ColumnExpression<T>, right: CharSequence) {
        context.add(Criterion.NotLike(Operand.Column(left), Operand.Argument(left.klass, right)))
    }

    override infix fun <T : Any> ColumnExpression<T>.eq(operand: ColumnExpression<T>) {
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.eq(operand: T?) {
        if (operand == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> T?.eq(operand: ColumnExpression<T>) {
        if (this == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.notEq(operand: ColumnExpression<T>) {
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.notEq(operand: T?) {
        if (operand == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> T?.notEq(operand: ColumnExpression<T>) {
        if (this == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.less(operand: ColumnExpression<T>) {
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.less(operand: T?) {
        if (operand == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> T?.less(operand: ColumnExpression<T>) {
        if (this == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.lessEq(operand: ColumnExpression<T>) {
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.lessEq(operand: T?) {
        if (operand == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> T?.lessEq(operand: ColumnExpression<T>) {
        if (this == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.greater(operand: ColumnExpression<T>) {
        add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.greater(operand: T?) {
        if (operand == null) return
        add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any> T?.greater(operand: ColumnExpression<T>) {
        if (this == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.greaterEq(operand: ColumnExpression<T>) {
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> ColumnExpression<T>.greaterEq(operand: T?) {
        if (operand == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> T?.greaterEq(operand: ColumnExpression<T>) {
        if (this == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override fun <T : Any> ColumnExpression<T>.isNull() {
        val left = Operand.Column(this)
        add(Criterion.IsNull(left))
    }

    override fun <T : Any> ColumnExpression<T>.isNotNull() {
        val left = Operand.Column(this)
        add(Criterion.IsNotNull(left))
    }

    override infix fun <T : CharSequence> ColumnExpression<T>.like(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand)
    }

    override infix fun <T : CharSequence> ColumnExpression<T>.notLike(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand)
    }

    override fun <T : CharSequence> ColumnExpression<T>.startsWith(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asPrefix())
    }

    override fun <T : CharSequence> ColumnExpression<T>.notStartsWith(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asPrefix())
    }

    override fun <T : CharSequence> ColumnExpression<T>.contains(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asInfix())
    }

    override fun <T : CharSequence> ColumnExpression<T>.notContains(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asInfix())
    }

    override fun <T : CharSequence> ColumnExpression<T>.endsWith(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asSuffix())
    }

    override fun <T : CharSequence> ColumnExpression<T>.notEndsWith(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asSuffix())
    }

    override infix fun <T : Comparable<T>> ColumnExpression<T>.between(range: ClosedRange<T>) {
        val left = Operand.Column(this)
        val right = Operand.Argument(this.klass, range.start) to Operand.Argument(this.klass, range.endInclusive)
        add(Criterion.Between(left, right))
    }

    override infix fun <T : Comparable<T>> ColumnExpression<T>.notBetween(range: ClosedRange<T>) {
        val left = Operand.Column(this)
        val right = Operand.Argument(this.klass, range.start) to Operand.Argument(this.klass, range.endInclusive)
        add(Criterion.NotBetween(left, right))
    }

    override infix fun <T : Any> ColumnExpression<T>.inList(values: List<T?>) {
        val o1 = Operand.Column(this)
        val o2 = values.map { Operand.Argument(this.klass, it) }
        add(Criterion.InList(o1, o2))
    }

    override infix fun <T : Any> ColumnExpression<T>.inList(block: () -> Subquery<T?>) {
        val subquery = block()
        val left = Operand.Column(this)
        val right = subquery.subqueryContext
        add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any> ColumnExpression<T>.notInList(values: List<T?>) {
        val o1 = Operand.Column(this)
        val o2 = values.map { Operand.Argument(this.klass, it) }
        add(Criterion.NotInList(o1, o2))
    }

    override infix fun <T : Any> ColumnExpression<T>.notInList(block: () -> Subquery<T?>) {
        val subquery = block()
        val left = Operand.Column(this)
        val right = subquery.subqueryContext
        add(Criterion.NotInSubQuery(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<ColumnExpression<A>, ColumnExpression<B>>.inList2(values: List<Pair<A?, B?>>) {
        val left = Operand.Column(this.first) to Operand.Column(this.second)
        val right = values.map {
            Operand.Argument(this.first.klass, it.first) to Operand.Argument(
                this.second.klass,
                it.second
            )
        }
        add(Criterion.InList2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<ColumnExpression<A>, ColumnExpression<B>>.inList2(block: () -> Subquery<Pair<A?, B?>>) {
        val subquery = block()
        val left = Operand.Column(this.first) to Operand.Column(this.second)
        val right = subquery.subqueryContext
        add(Criterion.InSubQuery2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<ColumnExpression<A>, ColumnExpression<B>>.notInList2(values: List<Pair<A?, B?>>) {
        val left = Operand.Column(this.first) to Operand.Column(this.second)
        val right = values.map {
            Operand.Argument(this.first.klass, it.first) to Operand.Argument(
                this.second.klass,
                it.second
            )
        }
        add(Criterion.NotInList2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<ColumnExpression<A>, ColumnExpression<B>>.notInList2(block: () -> Subquery<Pair<A?, B?>>) {
        val subquery = block()
        val left = Operand.Column(this.first) to Operand.Column(this.second)
        val right = subquery.subqueryContext
        add(Criterion.NotInSubQuery2(left, right))
    }

    override fun exists(block: () -> Subquery<*>) {
        val subquery = block()
        add(Criterion.Exists(subquery.subqueryContext))
    }

    override fun notExists(block: () -> Subquery<*>) {
        val subquery = block()
        add(Criterion.NotExists(subquery.subqueryContext))
    }
}
