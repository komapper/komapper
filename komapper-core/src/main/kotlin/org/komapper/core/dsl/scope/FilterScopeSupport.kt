package org.komapper.core.dsl.scope

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.query.Subquery

internal class FilterScopeSupport(
    private val context: MutableList<Criterion> = mutableListOf()
) : FilterScope, List<Criterion> by context {

    internal fun add(criterion: Criterion) {
        context.add(criterion)
    }

    private fun <T : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: PropertyExpression<T>,
        right: PropertyExpression<T>
    ) {
        context.add(operator(Operand.Property(left), Operand.Property(right)))
    }

    private fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: PropertyExpression<T>, right: T) {
        context.add(operator(Operand.Property(left), Operand.Parameter(left, right)))
    }

    private fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: T, right: PropertyExpression<T>) {
        context.add(operator(Operand.Parameter(right, left), Operand.Property(right)))
    }

    private fun <T : CharSequence> addLikeOperator(left: PropertyExpression<T>, right: CharSequence) {
        context.add(Criterion.Like(Operand.Property(left), Operand.Parameter(left, right)))
    }

    private fun <T : CharSequence> addNotLikeOperator(left: PropertyExpression<T>, right: CharSequence) {
        context.add(Criterion.NotLike(Operand.Property(left), Operand.Parameter(left, right)))
    }

    override infix fun <T : Any> PropertyExpression<T>.eq(operand: PropertyExpression<T>) {
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.eq(operand: T?) {
        if (operand == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> T?.eq(operand: PropertyExpression<T>) {
        if (this == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.notEq(operand: PropertyExpression<T>) {
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.notEq(operand: T?) {
        if (operand == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> T?.notEq(operand: PropertyExpression<T>) {
        if (this == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.less(operand: PropertyExpression<T>) {
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.less(operand: T?) {
        if (operand == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> T?.less(operand: PropertyExpression<T>) {
        if (this == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.lessEq(operand: PropertyExpression<T>) {
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.lessEq(operand: T?) {
        if (operand == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> T?.lessEq(operand: PropertyExpression<T>) {
        if (this == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.greater(operand: PropertyExpression<T>) {
        add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.greater(operand: T?) {
        if (operand == null) return
        add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any> T?.greater(operand: PropertyExpression<T>) {
        if (this == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.greaterEq(operand: PropertyExpression<T>) {
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> PropertyExpression<T>.greaterEq(operand: T?) {
        if (operand == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> T?.greaterEq(operand: PropertyExpression<T>) {
        if (this == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override fun <T : Any> PropertyExpression<T>.isNull() {
        val left = Operand.Property(this)
        add(Criterion.IsNull(left))
    }

    override fun <T : Any> PropertyExpression<T>.isNotNull() {
        val left = Operand.Property(this)
        add(Criterion.IsNotNull(left))
    }

    override infix fun <T : CharSequence> PropertyExpression<T>.like(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand)
    }

    override infix fun <T : CharSequence> PropertyExpression<T>.notLike(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand)
    }

    override fun <T : CharSequence> PropertyExpression<T>.startsWith(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, escape(operand) + text("%"))
    }

    override fun <T : CharSequence> PropertyExpression<T>.notStartsWith(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, escape(operand) + text("%"))
    }

    override fun <T : CharSequence> PropertyExpression<T>.contains(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, text("%") + escape(operand) + text("%"))
    }

    override fun <T : CharSequence> PropertyExpression<T>.notContains(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, text("%") + escape(operand) + text("%"))
    }

    override fun <T : CharSequence> PropertyExpression<T>.endsWith(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, text("%") + escape(operand))
    }

    override fun <T : CharSequence> PropertyExpression<T>.notEndsWith(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, text("%") + escape(operand))
    }

    override infix fun <T : Comparable<T>> PropertyExpression<T>.between(range: ClosedRange<T>) {
        val left = Operand.Property(this)
        val right = Operand.Parameter(this, range.start) to Operand.Parameter(this, range.endInclusive)
        add(Criterion.Between(left, right))
    }

    override infix fun <T : Comparable<T>> PropertyExpression<T>.notBetween(range: ClosedRange<T>) {
        val left = Operand.Property(this)
        val right = Operand.Parameter(this, range.start) to Operand.Parameter(this, range.endInclusive)
        add(Criterion.NotBetween(left, right))
    }

    override infix fun <T : Any> PropertyExpression<T>.inList(values: List<T?>) {
        val o1 = Operand.Property(this)
        val o2 = values.map { Operand.Parameter(this, it) }
        add(Criterion.InList(o1, o2))
    }

    override infix fun <T : Any> PropertyExpression<T>.inList(block: () -> Subquery<T?>) {
        val subquery = block()
        val left = Operand.Property(this)
        val right = subquery.subqueryContext
        add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any> PropertyExpression<T>.notInList(values: List<T?>) {
        val o1 = Operand.Property(this)
        val o2 = values.map { Operand.Parameter(this, it) }
        add(Criterion.NotInList(o1, o2))
    }

    override infix fun <T : Any> PropertyExpression<T>.notInList(block: () -> Subquery<T?>) {
        val subquery = block()
        val left = Operand.Property(this)
        val right = subquery.subqueryContext
        add(Criterion.NotInSubQuery(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<PropertyExpression<A>, PropertyExpression<B>>.inList2(values: List<Pair<A?, B?>>) {
        val left = Operand.Property(this.first) to Operand.Property(this.second)
        val right = values.map { Operand.Parameter(this.first, it.first) to Operand.Parameter(this.second, it.second) }
        add(Criterion.InList2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<PropertyExpression<A>, PropertyExpression<B>>.inList2(block: () -> Subquery<Pair<A?, B?>>) {
        val subquery = block()
        val left = Operand.Property(this.first) to Operand.Property(this.second)
        val right = subquery.subqueryContext
        add(Criterion.InSubQuery2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<PropertyExpression<A>, PropertyExpression<B>>.notInList2(values: List<Pair<A?, B?>>) {
        val left = Operand.Property(this.first) to Operand.Property(this.second)
        val right = values.map { Operand.Parameter(this.first, it.first) to Operand.Parameter(this.second, it.second) }
        add(Criterion.NotInList2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<PropertyExpression<A>, PropertyExpression<B>>.notInList2(block: () -> Subquery<Pair<A?, B?>>) {
        val subquery = block()
        val left = Operand.Property(this.first) to Operand.Property(this.second)
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
