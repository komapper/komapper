package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

/**
 * The builder of upsert queries.
 *
 * @param ENTITY the entity type
 * @param ID the entity id type
 * @param META the entity metamodel type
 */
@ThreadSafe
internal interface EntityUpsertQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun multiple(entities: List<ENTITY>): EntityUpsertMultipleQuery<ENTITY>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityUpsertQuery<List<Long>>
}

internal interface EntityUpsertQueryBuilderReturningSingle<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : EntityUpsertQueryBuilder<ENTITY, ID, META> {
    fun single(entity: ENTITY): EntityUpsertSingleQuery<ENTITY>
}

internal interface EntityUpsertQueryBuilderReturningSingleOrNull<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : EntityUpsertQueryBuilder<ENTITY, ID, META> {
    fun single(entity: ENTITY): EntityUpsertSingleQuery<ENTITY?>
}

internal data class EntityUpsertQueryBuilderReturningSingleImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
) : EntityUpsertQueryBuilderReturningSingle<ENTITY, ID, META> {

    override fun single(entity: ENTITY): EntityUpsertSingleQuery<ENTITY> {
        return EntityUpsertSingleQueryImpl(context, entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityUpsertMultipleQuery<ENTITY> {
        return EntityUpsertMultipleQueryImpl(context, entities)
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Long>> {
        return batch(context, entities, batchSize)
    }
}

internal data class EntityUpsertQueryBuilderReturningSingleOrNullImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
) : EntityUpsertQueryBuilderReturningSingleOrNull<ENTITY, ID, META> {

    override fun single(entity: ENTITY): EntityUpsertSingleQuery<ENTITY?> {
        return EntityUpsertSingleQueryImpl(context, entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityUpsertMultipleQuery<ENTITY> {
        return EntityUpsertMultipleQueryImpl(context, entities)
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Long>> {
        return batch(context, entities, batchSize)
    }
}

private fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> batch(context: EntityUpsertContext<ENTITY, ID, META>, entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Long>> {
    val newContext = if (batchSize != null) {
        val options = context.insertContext.options.copy(batchSize = batchSize)
        val insertContext = context.insertContext.copy(options = options)
        context.copy(insertContext = insertContext)
    } else {
        context
    }
    return EntityUpsertBatchQuery(newContext, entities)
}
