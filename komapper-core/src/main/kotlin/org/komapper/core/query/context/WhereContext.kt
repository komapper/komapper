package org.komapper.core.query.context

import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.Operand
import org.komapper.core.query.option.LikeOption

internal class WhereContext(private val criteria: MutableList<Criterion> = mutableListOf()) :
    Collection<Criterion> by criteria {

    fun add(criterion: Criterion) {
        criteria.add(criterion)
    }

    fun <T : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: PropertyMetamodel<*, T>,
        right: PropertyMetamodel<*, T>
    ) {
        criteria.add(operator(Operand.Property(left), Operand.Property(right)))
    }

    fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: PropertyMetamodel<*, T>, right: T) {
        criteria.add(operator(Operand.Property(left), Operand.Parameter(left, right)))
    }

    fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: T, right: PropertyMetamodel<*, T>) {
        criteria.add(operator(Operand.Parameter(right, left), Operand.Property(right)))
    }

    fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: PropertyMetamodel<*, T>,
        right: PropertyMetamodel<*, T>,
        option: LikeOption
    ) {
        criteria.add(operator(Operand.Property(left), Operand.Property(right), option))
    }

    fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: PropertyMetamodel<*, T>,
        right: Any,
        option: LikeOption
    ) {
        criteria.add(operator(Operand.Property(left), Operand.Parameter(left, right), option))
    }

    fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: Any,
        right: PropertyMetamodel<*, T>,
        option: LikeOption
    ) {
        criteria.add(operator(Operand.Parameter(right, left), Operand.Property(right), option))
    }
}
