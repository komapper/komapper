package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityUpdateQuery<T> : Query<T> {
    fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateQuery<T>
}

internal data class EntityUpdateSingleQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val options: UpdateOptions,
    private val entity: ENTITY
) : EntityUpdateQuery<ENTITY> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateSingleQuery(context, options, entity)
    }
}

internal data class EntityUpdateBatchQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val options: UpdateOptions,
    private val entities: List<ENTITY>,
) : EntityUpdateQuery<List<ENTITY>> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateQuery<List<ENTITY>> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateBatchQuery(context, options, entities)
    }
}
