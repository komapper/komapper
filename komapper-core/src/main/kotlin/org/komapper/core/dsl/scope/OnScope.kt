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

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.eq(right: PropertyMetamodel<ENTITY, T, S>) {
        context.add(Criterion.Eq(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.eq(right: T) {
        context.add(Criterion.Eq(Operand.Column(this), Operand.ExteriorArgument(this, right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.notEq(right: PropertyMetamodel<ENTITY, T, S>) {
        context.add(Criterion.NotEq(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.notEq(right: T) {
        context.add(Criterion.NotEq(Operand.Column(this), Operand.ExteriorArgument(this, right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.less(right: PropertyMetamodel<ENTITY, T, S>) {
        context.add(Criterion.Less(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.less(right: T) {
        context.add(Criterion.Less(Operand.Column(this), Operand.ExteriorArgument(this, right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.lessEq(right: PropertyMetamodel<ENTITY, T, S>) {
        context.add(Criterion.LessEq(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.lessEq(right: T) {
        context.add(Criterion.LessEq(Operand.Column(this), Operand.ExteriorArgument(this, right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.grater(right: PropertyMetamodel<ENTITY, T, S>) {
        context.add(Criterion.Grater(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.grater(right: T) {
        context.add(Criterion.Grater(Operand.Column(this), Operand.ExteriorArgument(this, right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.graterEq(right: PropertyMetamodel<ENTITY, T, S>) {
        context.add(Criterion.GraterEq(Operand.Column(this), Operand.Column(right)))
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<*, T, S>.graterEq(right: T) {
        context.add(Criterion.GraterEq(Operand.Column(this), Operand.ExteriorArgument(this, right)))
    }

    fun <T : Any, S : Any> ColumnExpression<T, S>.isNull() {
        val left = Operand.Column(this)
        context.add(Criterion.IsNull(left))
    }

    fun <T : Any, S : Any> ColumnExpression<T, S>.isNotNull() {
        val left = Operand.Column(this)
        context.add(Criterion.IsNotNull(left))
    }
}
