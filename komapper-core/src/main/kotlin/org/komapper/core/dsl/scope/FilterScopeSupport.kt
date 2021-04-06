package org.komapper.core.dsl.scope

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.operand.LikeOperand
import org.komapper.core.dsl.option.LikeOption
import org.komapper.core.dsl.query.SingleColumnSqlSubqueryResult
import org.komapper.core.dsl.query.SqlSubqueryResult
import org.komapper.core.metamodel.Column

internal class FilterScopeSupport(
    private val context: MutableList<Criterion> = mutableListOf()
) : FilterScope, List<Criterion> by context {

    internal fun add(criterion: Criterion) {
        context.add(criterion)
    }

    private fun <T : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: Column<T>,
        right: Column<T>
    ) {
        context.add(operator(Operand.Column(left), Operand.Column(right)))
    }

    private fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: Column<T>, right: T) {
        context.add(operator(Operand.Column(left), Operand.Parameter(left, right)))
    }

    private fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: T, right: Column<T>) {
        context.add(operator(Operand.Parameter(right, left), Operand.Column(right)))
    }

    private fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: Column<T>,
        right: Column<T>,
        option: LikeOption
    ) {
        context.add(operator(Operand.Column(left), Operand.Column(right), option))
    }

    private fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: Column<T>,
        right: Any,
        option: LikeOption
    ) {
        context.add(operator(Operand.Column(left), Operand.Parameter(left, right), option))
    }

    private fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: Any,
        right: Column<T>,
        option: LikeOption
    ) {
        context.add(operator(Operand.Parameter(right, left), Operand.Column(right), option))
    }

    override infix fun <T : Any> Column<T>.eq(operand: Column<T>) {
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> Column<T>.eq(operand: T?) {
        if (operand == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> T?.eq(operand: Column<T>) {
        if (this == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> Column<T>.notEq(operand: Column<T>) {
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> Column<T>.notEq(operand: T?) {
        if (operand == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> T?.notEq(operand: Column<T>) {
        if (this == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> Column<T>.less(operand: Column<T>) {
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> Column<T>.less(operand: T?) {
        if (operand == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> T?.less(operand: Column<T>) {
        if (this == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> Column<T>.lessEq(operand: Column<T>) {
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> Column<T>.lessEq(operand: T?) {
        if (operand == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> T?.lessEq(operand: Column<T>) {
        if (this == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> Column<T>.greater(operand: Column<T>) {
        add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any> Column<T>.greater(operand: T?) {
        if (operand == null) return
        add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any> T?.greater(operand: Column<T>) {
        if (this == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> Column<T>.greaterEq(operand: Column<T>) {
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> Column<T>.greaterEq(operand: T?) {
        if (operand == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> T?.greaterEq(operand: Column<T>) {
        if (this == null) return
        add(Criterion::GraterEq, this, operand)
    }

    override fun <T : Any> Column<T>.isNull() {
        val left = Operand.Column(this)
        add(Criterion.IsNull(left))
    }

    override fun <T : Any> Column<T>.isNotNull() {
        val left = Operand.Column(this)
        add(Criterion.IsNotNull(left))
    }

    override infix fun <T : CharSequence> Column<T>.like(operand: Any?) {
        if (operand == null) return
        add(Criterion::Like, this, operand, LikeOption.None)
    }

    override infix fun <T : CharSequence> Column<T>.like(operand: LikeOperand) {
        val value = operand.value ?: return
        val option = createLikeOption(operand)
        add(Criterion::Like, this, value, option)
    }

    override infix fun <T : CharSequence> Column<T>.notLike(operand: Any?) {
        if (operand == null) return
        add(Criterion::NotLike, this, operand, LikeOption.None)
    }

    override infix fun <T : CharSequence> Column<T>.notLike(operand: LikeOperand) {
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

    override infix fun <T : Comparable<T>> Column<T>.between(range: ClosedRange<T>) {
        val left = Operand.Column(this)
        val right = Operand.Parameter(this, range.start) to Operand.Parameter(this, range.endInclusive)
        add(Criterion.Between(left, right))
    }

    override infix fun <T : Comparable<T>> Column<T>.notBetween(range: ClosedRange<T>) {
        val left = Operand.Column(this)
        val right = Operand.Parameter(this, range.start) to Operand.Parameter(this, range.endInclusive)
        add(Criterion.NotBetween(left, right))
    }

    override infix fun <T : Any> Column<T>.inList(values: List<T?>) {
        val o1 = Operand.Column(this)
        val o2 = values.map { Operand.Parameter(this, it) }
        add(Criterion.InList(o1, o2))
    }

    override infix fun <T : Any> Column<T>.inList(block: () -> SingleColumnSqlSubqueryResult) {
        this.inList(block())
    }

    override infix fun <T : Any> Column<T>.inList(projection: SingleColumnSqlSubqueryResult) {
        val left = Operand.Column(this)
        val right = projection.contextHolder.context
        add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any> Column<T>.notInList(values: List<T?>) {
        val o1 = Operand.Column(this)
        val o2 = values.map { Operand.Parameter(this, it) }
        add(Criterion.NotInList(o1, o2))
    }

    override infix fun <T : Any> Column<T>.notInList(block: () -> SingleColumnSqlSubqueryResult) {
        this.notInList(block())
    }

    override infix fun <T : Any> Column<T>.notInList(projection: SingleColumnSqlSubqueryResult) {
        val left = Operand.Column(this)
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
