package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityContextQuery<ENTITY> : Query<EntityContext<ENTITY>> {
    fun include(metamodel: EntityMetamodel<*, *, *>): EntityContextQuery<ENTITY>
    fun options(configure: (SelectOptions) -> SelectOptions): EntityContextQuery<ENTITY>
}

internal data class EntityContextQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val options: SelectOptions
) : EntityContextQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' parameter is not found. Bind it to this query in advance by using the join clause."
        }
    }

    override fun include(metamodel: EntityMetamodel<*, *, *>): EntityContextQuery<ENTITY> {
        val metamodels = context.joins.map { it.target }
        require(metamodel in metamodels) { entityMetamodelNotFound("metamodel") }
        val newContext = context.addProjectionMetamodels(listOf(metamodel))
        return copy(context = newContext)
    }

    fun includeAll(): EntityContextQuery<ENTITY> {
        val newContext = context.addProjectionMetamodels(context.joins.map { it.target })
        return copy(context = newContext)
    }

    override fun options(configure: (SelectOptions) -> SelectOptions): EntityContextQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityContextQuery(context, options)
    }
}
