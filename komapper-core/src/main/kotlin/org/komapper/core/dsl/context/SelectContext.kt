package org.komapper.core.dsl.context

import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where

sealed interface SelectContext<
    ENTITY : Any,
    ID,
    META : EntityMetamodel<ENTITY, ID, META>,
    CONTEXT : SelectContext<ENTITY, ID, META, CONTEXT>> : QueryContext, SubqueryContext {

    val target: META
    val projection: Projection
    val joins: List<Join<*, *, *>>
    val where: List<WhereDeclaration>
    val orderBy: List<SortItem>
    val offset: Int
    val limit: Int
    val forUpdate: ForUpdate
    val distinct: Boolean

    fun addJoin(join: Join<*, *, *>): CONTEXT
    fun addWhere(where: WhereDeclaration): CONTEXT
    fun addOrderBy(orderBy: List<SortItem>): CONTEXT
    fun setLimit(limit: Int): CONTEXT
    fun setOffset(offset: Int): CONTEXT
    fun setForUpdate(forUpdate: ForUpdate): CONTEXT

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target) + joins.map { it.target }
    }

    override fun getWhereDeclarations(): List<WhereDeclaration> {
        return target.where + joins.flatMap { it.getWhereDeclarations() } + where
    }
}
