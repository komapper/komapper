package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.PropertyExpression
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
        context.add(Criterion.Eq(Operand.Property(this), Operand.Property(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.eq(right: T) {
        context.add(Criterion.Eq(Operand.Property(this), Operand.Parameter(this, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.notEq(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.NotEq(Operand.Property(this), Operand.Property(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.notEq(right: T) {
        context.add(Criterion.NotEq(Operand.Property(this), Operand.Parameter(this, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.less(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.Less(Operand.Property(this), Operand.Property(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.less(right: T) {
        context.add(Criterion.Less(Operand.Property(this), Operand.Parameter(this, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.lessEq(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.LessEq(Operand.Property(this), Operand.Property(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.lessEq(right: T) {
        context.add(Criterion.LessEq(Operand.Property(this), Operand.Parameter(this, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.grater(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.Grater(Operand.Property(this), Operand.Property(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.grater(right: T) {
        context.add(Criterion.Grater(Operand.Property(this), Operand.Parameter(this, right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.graterEq(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.GraterEq(Operand.Property(this), Operand.Property(right)))
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.graterEq(right: T) {
        context.add(Criterion.GraterEq(Operand.Property(this), Operand.Parameter(this, right)))
    }

    fun <T : Any> PropertyExpression<T>.isNull() {
        val left = Operand.Property(this)
        context.add(Criterion.IsNull(left))
    }

    fun <T : Any> PropertyExpression<T>.isNotNull() {
        val left = Operand.Property(this)
        context.add(Criterion.IsNotNull(left))
    }
}
