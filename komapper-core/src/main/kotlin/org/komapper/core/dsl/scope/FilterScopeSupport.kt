package org.komapper.core.dsl.scope

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.operand.LikeOperand
import org.komapper.core.dsl.option.LikeOption
import org.komapper.core.dsl.query.SingleColumnSqlSubqueryResult
import org.komapper.core.dsl.query.SqlSubqueryResult

internal class FilterScopeSupport(
    private val context: MutableList<Criterion> = mutableListOf()
) : FilterScope, Collection<Criterion> by context {

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

    private fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: PropertyExpression<T>,
        right: PropertyExpression<T>,
        option: LikeOption
    ) {
        context.add(operator(Operand.Property(left), Operand.Property(right), option))
    }

    private fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: PropertyExpression<T>,
        right: Any,
        option: LikeOption
    ) {
        context.add(operator(Operand.Property(left), Operand.Parameter(left, right), option))
    }

    private fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: Any,
        right: PropertyExpression<T>,
        option: LikeOption
    ) {
        context.add(operator(Operand.Parameter(right, left), Operand.Property(right), option))
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

    override infix fun <T : CharSequence> PropertyExpression<T>.like(operand: Any?) {
        if (operand == null) return
        add(Criterion::Like, this, operand, LikeOption.None)
    }

    override infix fun <T : CharSequence> PropertyExpression<T>.like(operand: LikeOperand) {
        val value = operand.value ?: return
        val option = createLikeOption(operand)
        add(Criterion::Like, this, value, option)
    }

    override infix fun <T : CharSequence> PropertyExpression<T>.notLike(operand: Any?) {
        if (operand == null) return
        add(Criterion::NotLike, this, operand, LikeOption.None)
    }

    override infix fun <T : CharSequence> PropertyExpression<T>.notLike(operand: LikeOperand) {
        val value = operand.value ?: return
        val option = createLikeOption(operand)
        add(Criterion::NotLike, this, value, option)
    }

    private fun createLikeOption(operand: LikeOperand): LikeOption {
        return when (operand) {
            is LikeOperand.Normal -> LikeOption.None
            is LikeOperand.Escape -> LikeOption.Escape(operand.escapeChar)
            is LikeOperand.Prefix -> LikeOption.Prefix(operand.escapeChar)
            is LikeOperand.Infix -> LikeOption.Infix(operand.escapeChar)
            is LikeOperand.Suffix -> LikeOption.Suffix(operand.escapeChar)
        }
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

    override infix fun <T : Any> PropertyExpression<T>.inList(block: () -> SingleColumnSqlSubqueryResult) {
        this.inList(block())
    }

    override infix fun <T : Any> PropertyExpression<T>.inList(projection: SingleColumnSqlSubqueryResult) {
        val left = Operand.Property(this)
        val right = projection.contextHolder.context
        add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any> PropertyExpression<T>.notInList(values: List<T?>) {
        val o1 = Operand.Property(this)
        val o2 = values.map { Operand.Parameter(this, it) }
        add(Criterion.NotInList(o1, o2))
    }

    override infix fun <T : Any> PropertyExpression<T>.notInList(block: () -> SingleColumnSqlSubqueryResult) {
        this.notInList(block())
    }

    override infix fun <T : Any> PropertyExpression<T>.notInList(projection: SingleColumnSqlSubqueryResult) {
        val left = Operand.Property(this)
        val right = projection.contextHolder.context
        add(Criterion.NotInSubQuery(left, right))
    }

    override fun exists(block: () -> SqlSubqueryResult) {
        this.exists(block())
    }

    override fun exists(result: SqlSubqueryResult) {
        val subContext = result.contextHolder.context
        add(Criterion.Exists(subContext))
    }

    override fun notExists(block: () -> SqlSubqueryResult) {
        this.notExists(block())
    }

    override fun notExists(result: SqlSubqueryResult) {
        val subContext = result.contextHolder.context
        add(Criterion.NotExists(subContext))
    }

    override fun <T : CharSequence> T?.escape(): LikeOperand {
        return LikeOperand.Escape(this)
    }

    override fun <T : CharSequence> T?.asPrefix(): LikeOperand {
        return LikeOperand.Prefix(this)
    }

    override fun <T : CharSequence> T?.asInfix(): LikeOperand {
        return LikeOperand.Infix(this)
    }

    override fun <T : CharSequence> T?.asSuffix(): LikeOperand {
        return LikeOperand.Suffix(this)
    }
}
