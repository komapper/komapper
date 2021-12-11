package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
interface EntityUpsertQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun single(entity: ENTITY): EntityUpsertQuery<Int>
    fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Int>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityUpsertQuery<List<Int>>
}

internal data class EntityUpsertQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
) : EntityUpsertQueryBuilder<ENTITY, ID, META> {

    override fun single(entity: ENTITY): EntityUpsertQuery<Int> {
        return EntityUpsertSingleQuery(context, entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Int> {
        return EntityUpsertMultipleQuery(context, entities)
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Int>> {
        val context = if (batchSize != null) {
            val options = context.insertContext.options.copy(batchSize = batchSize)
            val insertContext = context.insertContext.copy(options = options)
            context.copy(insertContext = insertContext)
        } else context

        return EntityUpsertBatchQuery(context, entities)
    }
}
