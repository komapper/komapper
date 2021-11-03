package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityAggregateQuery : Query<EntityAggregate> {

    fun <T : Any, S : Any> associate(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>,
    ): EntityAggregateQuery

    fun options(configure: (EntitySelectOptions) -> EntitySelectOptions): EntityAggregateQuery
}

data class AggregateQueryImpl(
    private val context: EntitySelectContext<*, *, *>,
    private val options: EntitySelectOptions
) : EntityAggregateQuery {

    override fun <T : Any, S : Any> associate(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>,
    ): EntityAggregateQuery {
        val metamodels = context.getEntityMetamodels()
        require(metamodel1 in metamodels) { EntitySelectQueryImpl.entityMetamodelNotFound("metamodel1") }
        require(metamodel2 in metamodels) { EntitySelectQueryImpl.entityMetamodelNotFound("metamodel2") }
        @Suppress("UNCHECKED_CAST")
        val newContext = context.addAssociation(metamodel1 to metamodel2)
        return copy(context = newContext)
    }

    override fun options(configure: (EntitySelectOptions) -> EntitySelectOptions): EntityAggregateQuery {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityAggregateQuery(context, options)
    }
}
