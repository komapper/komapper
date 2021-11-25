package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityStoreQuery<ENTITY> : Query<EntityStore<ENTITY>> {
    fun options(configure: (SelectOptions) -> SelectOptions): EntityStoreQuery<ENTITY>
}

internal data class EntityStoreQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
    private val options: SelectOptions
) : EntityStoreQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, ID, META> = SelectQuerySupport(context)

    fun include(vararg metamodels: EntityMetamodel<*, *, *>): EntityStoreQuery<ENTITY> {
        val newContext = support.include(metamodels.toList())
        return copy(context = newContext)
    }

    fun includeAll(): EntityStoreQuery<ENTITY> {
        val newContext = support.includeAll()
        return copy(context = newContext)
    }

    override fun options(configure: (SelectOptions) -> SelectOptions): EntityStoreQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityContextQuery(context, options)
    }
}
