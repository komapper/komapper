package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ForUpdateDeclaration
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.SelectOptions

internal class SelectQuerySupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
) {

    fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> join(
        join: Join<ENTITY2, ID2, META2>,
    ): SelectContext<ENTITY, ID, META> {
        return context.copy(joins = context.joins + join)
    }

    fun where(declaration: WhereDeclaration): SelectContext<ENTITY, ID, META> {
        return context.copy(where = context.where + declaration)
    }

    fun orderBy(expressions: List<SortExpression>): SelectContext<ENTITY, ID, META> {
        val items = expressions.map(SortItem.Column::of)
        return context.copy(orderBy = context.orderBy + items)
    }

    fun offset(offset: Int): SelectContext<ENTITY, ID, META> {
        return context.copy(offset = offset)
    }

    fun limit(limit: Int): SelectContext<ENTITY, ID, META> {
        return context.copy(limit = limit)
    }

    fun forUpdate(declaration: ForUpdateDeclaration): SelectContext<ENTITY, ID, META> {
        return context.copy(forUpdate = declaration)
    }

    fun select(vararg expressions: ColumnExpression<*, *>): SelectContext<ENTITY, ID, META> {
        return context.copy(select = expressions.toList())
    }

    fun include(metamodels: List<EntityMetamodel<*, *, *>>): SelectContext<ENTITY, ID, META> {
        return context.copy(include = context.include + metamodels)
    }

    fun includeAll(): SelectContext<ENTITY, ID, META> {
        return context.copy(includeAll = true)
    }

    fun options(options: SelectOptions): SelectContext<ENTITY, ID, META> {
        return context.copy(options = options)
    }
}
