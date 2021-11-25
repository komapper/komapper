package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.HavingDeclaration
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where
import org.komapper.core.dsl.operator.plus

data class SelectContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val select: List<ColumnExpression<*, *>> = listOf(),
    val joins: List<Join<*, *, *>> = listOf(),
    val where: WhereDeclaration = {},
    val orderBy: List<SortItem> = listOf(),
    val offset: Int = -1,
    val limit: Int = -1,
    val forUpdate: ForUpdate = ForUpdate(),
    val distinct: Boolean = false,
    val groupBy: List<ColumnExpression<*, *>> = listOf(),
    val having: HavingDeclaration = {},
    val include: List<EntityMetamodel<*, *, *>> = listOf(),
    val includeAll: Boolean = false,
) : QueryContext, SubqueryContext {

    fun getProjection(): Projection {
        return if (select.isNotEmpty()) {
            Projection.Expressions(select)
        } else {
            val joinedMetamodels = joins.map { it.target }
            val includedMetamodels = if (includeAll) joinedMetamodels else include.filter { it in joinedMetamodels }
            val projectionMetamodels = (setOf(target) + includedMetamodels)
            Projection.Metamodels(projectionMetamodels)
        }
    }

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target) + joins.map { it.target }
    }

    override fun getCompositeWhere(): WhereDeclaration {
        val where = joins.map { it.where }.reduceOrNull { acc, w -> acc + w } ?: {}
        return target.where + where + this.where
    }
}
