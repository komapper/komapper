package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
interface EntityDeleteQueryBuilder<ENTITY : Any> {
    fun single(entity: ENTITY): EntityDeleteQuery
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityDeleteQuery
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityDeleteQuery
    fun where(declaration: WhereDeclaration): RelationDeleteQuery
    fun all(): RelationDeleteQuery
}

internal data class EntityDeleteQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
) :
    EntityDeleteQueryBuilder<ENTITY> {

    override fun single(entity: ENTITY): EntityDeleteQuery {
        context.target.checkIdValueNotNull(entity)
        return EntityDeleteSingleQuery(context, entity)
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityDeleteQuery {
        context.target.checkIdValueNotNull(entities)
        val context = if (batchSize != null) {
            context.copy(options = context.options.copy(batchSize = batchSize))
        } else context
        return EntityDeleteBatchQuery(context, entities)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityDeleteQuery {
        return batch(entities.toList(), batchSize)
    }

    override fun where(declaration: WhereDeclaration): RelationDeleteQuery {
        return asRelationDeleteQuery().where(declaration)
    }

    override fun all(): RelationDeleteQuery {
        return asRelationDeleteQuery()
    }

    private fun asRelationDeleteQuery(): RelationDeleteQuery {
        return RelationDeleteQueryImpl(context.asRelationDeleteContext())
    }
}
