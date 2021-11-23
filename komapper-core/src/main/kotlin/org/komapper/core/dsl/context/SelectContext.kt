package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.HavingDeclaration
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where
import org.komapper.core.dsl.operator.plus

data class SelectContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val projection: Projection = Projection.Metamodels(listOf(target)),
    val joins: List<Join<*, *, *>> = listOf(),
    val where: WhereDeclaration = {},
    val orderBy: List<SortItem> = listOf(),
    val offset: Int = -1,
    val limit: Int = -1,
    val forUpdate: ForUpdate = ForUpdate(),
    val distinct: Boolean = false,
    val groupBy: List<ColumnExpression<*, *>> = listOf(),
    val having: HavingDeclaration = {},
) : QueryContext, SubqueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target) + joins.map { it.target }
    }

    override fun getCompositeWhere(): WhereDeclaration {
        val where = joins.map { it.where }.reduceOrNull { acc, w -> acc + w } ?: {}
        return target.where + where + this.where
    }
}
