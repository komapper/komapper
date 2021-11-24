package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityContextQuery<ENTITY> : Query<EntityContext<ENTITY>> {
    fun options(configure: (SelectOptions) -> SelectOptions): EntityContextQuery<ENTITY>
}

internal data class EntityContextQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
    private val options: SelectOptions
) : EntityContextQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, ID, META> = SelectQuerySupport(context)

    fun include(vararg metamodels: EntityMetamodel<*, *, *>): EntityContextQuery<ENTITY> {
        val joinedMetamodels = context.joins.map { it.target }
        val projectionMetamodels = metamodels.filter { it in joinedMetamodels }
        val newContext = support.addProjection(projectionMetamodels)
        return copy(context = newContext)
    }

    fun includeAll(): EntityContextQuery<ENTITY> {
        val metamodels = context.joins.map { it.target }
        val newContext = support.addProjection(metamodels)
        return copy(context = newContext)
    }

    override fun options(configure: (SelectOptions) -> SelectOptions): EntityContextQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityContextQuery(context, options)
    }
}
