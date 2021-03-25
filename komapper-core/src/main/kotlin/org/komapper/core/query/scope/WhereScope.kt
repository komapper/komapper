package org.komapper.core.query.scope

import org.komapper.core.Scope
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.WhereContext
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.Operand
import org.komapper.core.query.option.LikeOption

@Scope
class WhereScope internal constructor(private val context: WhereContext) {

    companion object {
        operator fun WhereDeclaration.plus(other: WhereDeclaration): WhereDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.eq(operand: PropertyMetamodel<*, T>) {
        context.add(Criterion::Eq, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.eq(operand: T?) {
        if (operand == null) return
        context.add(Criterion::Eq, this, operand)
    }

    infix fun <T : Any> T?.eq(operand: PropertyMetamodel<*, T>) {
        if (this == null) return
        context.add(Criterion::Eq, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.notEq(operand: PropertyMetamodel<*, T>) {
        context.add(Criterion::NotEq, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.notEq(operand: T?) {
        if (operand == null) return
        context.add(Criterion::NotEq, this, operand)
    }

    infix fun <T : Any> T?.notEq(operand: PropertyMetamodel<*, T>) {
        if (this == null) return
        context.add(Criterion::NotEq, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.less(operand: PropertyMetamodel<*, T>) {
        context.add(Criterion::Less, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.less(operand: T?) {
        if (operand == null) return
        context.add(Criterion::Less, this, operand)
    }

    infix fun <T : Any> T?.less(operand: PropertyMetamodel<*, T>) {
        if (this == null) return
        context.add(Criterion::Less, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.lessEq(operand: PropertyMetamodel<*, T>) {
        context.add(Criterion::LessEq, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.lessEq(operand: T?) {
        if (operand == null) return
        context.add(Criterion::LessEq, this, operand)
    }

    infix fun <T : Any> T?.lessEq(operand: PropertyMetamodel<*, T>) {
        if (this == null) return
        context.add(Criterion::LessEq, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.greater(operand: PropertyMetamodel<*, T>) {
        context.add(Criterion::Grater, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.greater(operand: T?) {
        if (operand == null) return
        context.add(Criterion::Grater, this, operand)
    }

    infix fun <T : Any> T?.greater(operand: PropertyMetamodel<*, T>) {
        if (this == null) return
        context.add(Criterion::GraterEq, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.greaterEq(operand: PropertyMetamodel<*, T>) {
        context.add(Criterion::GraterEq, this, operand)
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.greaterEq(operand: T?) {
        if (operand == null) return
        context.add(Criterion::GraterEq, this, operand)
    }

    infix fun <T : Any> T?.greaterEq(operand: PropertyMetamodel<*, T>) {
        if (this == null) return
        context.add(Criterion::GraterEq, this, operand)
    }

    fun <T : Any> PropertyMetamodel<*, T>.isNull() {
        val left = Operand.Property(this)
        context.add(Criterion.IsNull(left))
    }

    fun <T : Any> PropertyMetamodel<*, T>.isNotNull() {
        val left = Operand.Property(this)
        context.add(Criterion.IsNotNull(left))
    }

    infix fun <T : CharSequence> PropertyMetamodel<*, T>.like(operand: Any?) {
        if (operand == null) return
        context.add(Criterion::Like, this, operand, LikeOption.None)
    }

    infix fun <T : CharSequence> PropertyMetamodel<*, T>.like(operand: LikeOperand) {
        val value = operand.value ?: return
        val option = createLikeOption(operand)
        context.add(Criterion::Like, this, value, option)
    }

    infix fun <T : CharSequence> PropertyMetamodel<*, T>.notLike(operand: Any?) {
        if (operand == null) return
        context.add(Criterion::NotLike, this, operand, LikeOption.None)
    }

    infix fun <T : CharSequence> PropertyMetamodel<*, T>.notLike(operand: LikeOperand) {
        val value = operand.value ?: return
        val option = createLikeOption(operand)
        context.add(Criterion::NotLike, this, value, option)
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

    infix fun <T : Comparable<T>> PropertyMetamodel<*, T>.between(range: ClosedRange<T>) {
        val left = Operand.Property(this)
        val right = Operand.Parameter(this, range.start) to Operand.Parameter(this, range.endInclusive)
        context.add(Criterion.Between(left, right))
    }

    infix fun <T : Comparable<T>> PropertyMetamodel<*, T>.notBetween(range: ClosedRange<T>) {
        val left = Operand.Property(this)
        val right = Operand.Parameter(this, range.start) to Operand.Parameter(this, range.endInclusive)
        context.add(Criterion.NotBetween(left, right))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.inList(values: List<T?>) {
        val o1 = Operand.Property(this)
        val o2 = values.map { Operand.Parameter(this, it) }
        context.add(Criterion.InList(o1, o2))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.notInList(values: List<T?>) {
        val o1 = Operand.Property(this)
        val o2 = values.map { Operand.Parameter(this, it) }
        context.add(Criterion.NotInList(o1, o2))
    }

    fun and(declaration: WhereDeclaration) {
        addCriteria(declaration, Criterion::And)
    }

    fun or(declaration: WhereDeclaration) {
        addCriteria(declaration, Criterion::Or)
    }

    fun not(declaration: WhereDeclaration) {
        addCriteria(declaration, Criterion::Not)
    }

    private fun addCriteria(declaration: WhereDeclaration, operator: (List<Criterion>) -> Criterion) {
        val criteria = mutableListOf<Criterion>()
        val subContext = WhereContext(criteria)
        val scope = WhereScope(subContext)
        declaration(scope)
        context.add(operator(criteria))
    }

    fun <T : CharSequence> T?.escape(): LikeOperand {
        return LikeOperand.Escape(this)
    }

    fun <T : CharSequence> T?.asPrefix(): LikeOperand {
        return LikeOperand.Prefix(this)
    }

    fun <T : CharSequence> T?.asInfix(): LikeOperand {
        return LikeOperand.Infix(this)
    }

    fun <T : CharSequence> T?.asSuffix(): LikeOperand {
        return LikeOperand.Suffix(this)
    }
}
