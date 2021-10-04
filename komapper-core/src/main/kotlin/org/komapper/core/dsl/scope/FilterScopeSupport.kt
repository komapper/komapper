package org.komapper.core.dsl.scope

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.SubqueryExpression

class FilterScopeSupport<T>(
    private val context: MutableList<Criterion> = mutableListOf(),
    private val newScope: () -> T
) : FilterScope, List<Criterion> by context
        where T : FilterScope,
              T : List<Criterion> {

    internal fun add(criterion: Criterion) {
        context.add(criterion)
    }

    private fun <T : Any, S : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: ColumnExpression<T, S>,
        right: ColumnExpression<T, S>
    ) {
        context.add(operator(Operand.Column(left), Operand.Column(right)))
    }

    private fun <T : Any, S : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: ColumnExpression<T, S>,
        right: T
    ) {
        context.add(operator(Operand.Column(left), Operand.Argument(left, right)))
    }

    private fun <T : Any, S : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: T,
        right: ColumnExpression<T, S>
    ) {
        context.add(operator(Operand.Argument(right, left), Operand.Column(right)))
    }

    private fun <T : Any, S : CharSequence> addLikeOperator(left: ColumnExpression<T, S>, right: EscapeExpression) {
        context.add(Criterion.Like(Operand.Column(left), right))
    }

    private fun <T : Any, S : CharSequence> addNotLikeOperator(left: ColumnExpression<T, S>, right: EscapeExpression) {
        context.add(Criterion.NotLike(Operand.Column(left), right))
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.eq(operand: ColumnExpression<T, S>) {
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.eq(operand: T?) {
        if (operand == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.eq(operand: ColumnExpression<T, S>) {
        if (this == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.notEq(operand: ColumnExpression<T, S>) {
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.notEq(operand: T?) {
        if (operand == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.notEq(operand: ColumnExpression<T, S>) {
        if (this == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.less(operand: ColumnExpression<T, S>) {
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.less(operand: T?) {
        if (operand == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.less(operand: ColumnExpression<T, S>) {
        if (this == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.lessEq(operand: ColumnExpression<T, S>) {
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.lessEq(operand: T?) {
        if (operand == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.lessEq(operand: ColumnExpression<T, S>) {
        if (this == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.greater(operand: ColumnExpression<T, S>) {
        add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.greater(operand: T?) {
        if (operand == null) return
        add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.greater(operand: ColumnExpression<T, S>) {
        if (this == null) return
        add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.greaterEq(operand: ColumnExpression<T, S>) {
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.greaterEq(operand: T?) {
        if (operand == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.greaterEq(operand: ColumnExpression<T, S>) {
        if (this == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override fun <T : Any, S : Any> ColumnExpression<T, S>.isNull() {
        val left = Operand.Column(this)
        add(Criterion.IsNull(left))
    }

    override fun <T : Any, S : Any> ColumnExpression<T, S>.isNotNull() {
        val left = Operand.Column(this)
        add(Criterion.IsNotNull(left))
    }

    override infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.like(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, text(operand))
    }

    override infix fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notLike(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, text(operand))
    }

    override fun <T : Any, S : CharSequence> ColumnExpression<T, S>.startsWith(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asPrefix())
    }

    override fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notStartsWith(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asPrefix())
    }

    override fun <T : Any, S : CharSequence> ColumnExpression<T, S>.contains(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asInfix())
    }

    override fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notContains(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asInfix())
    }

    override fun <T : Any, S : CharSequence> ColumnExpression<T, S>.endsWith(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asSuffix())
    }

    override fun <T : Any, S : CharSequence> ColumnExpression<T, S>.notEndsWith(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asSuffix())
    }

    override infix fun <T : Comparable<T>, S : Any> ColumnExpression<T, S>.between(range: ClosedRange<T>) {
        val left = Operand.Column(this)
        val right = Operand.Argument(this, range.start) to Operand.Argument(this, range.endInclusive)
        add(Criterion.Between(left, right))
    }

    override infix fun <T : Comparable<T>, S : Any> ColumnExpression<T, S>.notBetween(range: ClosedRange<T>) {
        val left = Operand.Column(this)
        val right = Operand.Argument(this, range.start) to Operand.Argument(this, range.endInclusive)
        add(Criterion.NotBetween(left, right))
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.inList(values: List<T?>) {
        val o1 = Operand.Column(this)
        val o2 = values.map { Operand.Argument(this, it) }
        add(Criterion.InList(o1, o2))
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.inList(block: () -> SubqueryExpression<T?>) {
        val subquery = block()
        val left = Operand.Column(this)
        val right = subquery.subqueryContext
        add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.notInList(values: List<T?>) {
        val o1 = Operand.Column(this)
        val o2 = values.map { Operand.Argument(this, it) }
        add(Criterion.NotInList(o1, o2))
    }

    override infix fun <T : Any, S : Any> ColumnExpression<T, S>.notInList(block: () -> SubqueryExpression<T?>) {
        val subquery = block()
        val left = Operand.Column(this)
        val right = subquery.subqueryContext
        add(Criterion.NotInSubQuery(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.inList2(values: List<Pair<A?, B?>>) {
        val left = Operand.Column(this.first) to Operand.Column(this.second)
        val right = values.map {
            Operand.Argument(this.first, it.first) to Operand.Argument(
                this.second,
                it.second
            )
        }
        add(Criterion.InList2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.inList2(block: () -> SubqueryExpression<Pair<A?, B?>>) {
        val subquery = block()
        val left = Operand.Column(this.first) to Operand.Column(this.second)
        val right = subquery.subqueryContext
        add(Criterion.InSubQuery2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.notInList2(values: List<Pair<A?, B?>>) {
        val left = Operand.Column(this.first) to Operand.Column(this.second)
        val right = values.map {
            Operand.Argument(this.first, it.first) to Operand.Argument(
                this.second,
                it.second
            )
        }
        add(Criterion.NotInList2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>.notInList2(block: () -> SubqueryExpression<Pair<A?, B?>>) {
        val subquery = block()
        val left = Operand.Column(this.first) to Operand.Column(this.second)
        val right = subquery.subqueryContext
        add(Criterion.NotInSubQuery2(left, right))
    }

    override fun exists(block: () -> SubqueryExpression<*>) {
        val subquery = block()
        add(Criterion.Exists(subquery.subqueryContext))
    }

    override fun notExists(block: () -> SubqueryExpression<*>) {
        val subquery = block()
        add(Criterion.NotExists(subquery.subqueryContext))
    }

    fun addCriteria(declaration: T.() -> Unit, operator: (List<Criterion>) -> Criterion) {
        val scope = newScope().apply(declaration)
        val criterion = operator(scope.toList())
        add(criterion)
    }
}
