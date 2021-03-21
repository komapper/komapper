package org.komapper.core.query.scope

import org.komapper.core.Scope
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.WhereContext
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.Operand

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
}
