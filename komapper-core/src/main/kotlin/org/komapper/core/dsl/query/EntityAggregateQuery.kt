package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityAggregateQuery<ENTITY> : Query<EntityAggregate<ENTITY>> {
    fun include(metamodel: EntityMetamodel<*, *, *>): EntityAggregateQuery<ENTITY>
    fun options(configure: (EntitySelectOptions) -> EntitySelectOptions): EntityAggregateQuery<ENTITY>
}

internal data class EntityAggregateQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val options: EntitySelectOptions
) : EntityAggregateQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' parameter is not found. Bind it to this query in advance by using the join clause."
        }
    }

    override fun include(metamodel: EntityMetamodel<*, *, *>): EntityAggregateQuery<ENTITY> {
        val metamodels = context.joins.map { it.target }
        require(metamodel in metamodels) { entityMetamodelNotFound("metamodel") }
        val newContext = context.addProjectionMetamodels(listOf(metamodel))
        return copy(context = newContext)
    }

    fun includeAll(): EntityAggregateQuery<ENTITY> {
        val newContext = context.addProjectionMetamodels(context.joins.map { it.target })
        return copy(context = newContext)
    }

    override fun options(configure: (EntitySelectOptions) -> EntitySelectOptions): EntityAggregateQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityAggregateQuery(context, options)
    }
}
