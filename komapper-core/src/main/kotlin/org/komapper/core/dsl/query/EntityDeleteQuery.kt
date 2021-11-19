package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityDeleteQuery : Query<Unit> {
    fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteQuery
}

internal data class EntityDeleteSingleQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val options: DeleteOptions,
    private val entity: ENTITY
) : EntityDeleteQuery {

    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteQuery {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteSingleQuery(context, options, entity)
    }
}

internal data class EntityDeleteBatchQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val options: DeleteOptions,
    private val entities: List<ENTITY>,
) : EntityDeleteQuery {

    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteQuery {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteBatchQuery(context, options, entities)
    }
}
