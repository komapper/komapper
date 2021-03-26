package org.komapper.core.query.context

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.Operand
import org.komapper.core.query.option.LikeOption

internal class FilterContext(private val criteria: MutableList<Criterion> = mutableListOf()) :
    Collection<Criterion> by criteria {

    fun add(criterion: Criterion) {
        criteria.add(criterion)
    }

    fun <T : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: ColumnInfo<T>,
        right: ColumnInfo<T>
    ) {
        criteria.add(operator(Operand.Column(left), Operand.Column(right)))
    }

    fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: ColumnInfo<T>, right: T) {
        criteria.add(operator(Operand.Column(left), Operand.Parameter(left, right)))
    }

    fun <T : Any> add(operator: (Operand, Operand) -> Criterion, left: T, right: ColumnInfo<T>) {
        criteria.add(operator(Operand.Parameter(right, left), Operand.Column(right)))
    }

    fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: ColumnInfo<T>,
        right: ColumnInfo<T>,
        option: LikeOption
    ) {
        criteria.add(operator(Operand.Column(left), Operand.Column(right), option))
    }

    fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: ColumnInfo<T>,
        right: Any,
        option: LikeOption
    ) {
        criteria.add(operator(Operand.Column(left), Operand.Parameter(left, right), option))
    }

    fun <T : Any> add(
        operator: (Operand, Operand, LikeOption) -> Criterion,
        left: Any,
        right: ColumnInfo<T>,
        option: LikeOption
    ) {
        criteria.add(operator(Operand.Parameter(right, left), Operand.Column(right), option))
    }
}
