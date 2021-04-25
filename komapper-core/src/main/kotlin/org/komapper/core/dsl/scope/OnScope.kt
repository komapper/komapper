package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.PropertyMetamodel

@Scope
class OnScope<ENTITY : Any> internal constructor(
    private val context: MutableList<Criterion> = mutableListOf()
) :
    Collection<Criterion> by context {

    companion object {
        operator fun <E : Any> OnDeclaration<E>.plus(other: OnDeclaration<E>): OnDeclaration<E> {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.eq(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.Eq(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.eq(right: T) {
        context.add(Criterion.Eq(Operand.Column(this), Operand.Argument(this.klass, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.notEq(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.NotEq(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.notEq(right: T) {
        context.add(Criterion.NotEq(Operand.Column(this), Operand.Argument(this.klass, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.less(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.Less(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.less(right: T) {
        context.add(Criterion.Less(Operand.Column(this), Operand.Argument(this.klass, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.lessEq(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.LessEq(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.lessEq(right: T) {
        context.add(Criterion.LessEq(Operand.Column(this), Operand.Argument(this.klass, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.grater(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.Grater(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.grater(right: T) {
        context.add(Criterion.Grater(Operand.Column(this), Operand.Argument(this.klass, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.graterEq(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.GraterEq(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.graterEq(right: T) {
        context.add(Criterion.GraterEq(Operand.Column(this), Operand.Argument(this.klass, right)))
    }

    fun <T : Any> ColumnExpression<T>.isNull() {
        val left = Operand.Column(this)
        context.add(Criterion.IsNull(left))
    }

    fun <T : Any> ColumnExpression<T>.isNotNull() {
        val left = Operand.Column(this)
        context.add(Criterion.IsNotNull(left))
    }
}
