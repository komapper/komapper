package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteOptions

@ThreadSafe
interface EntityDeleteQueryBuilder<ENTITY : Any> {
    fun options(configure: (EntityDeleteOptions) -> EntityDeleteOptions): EntityDeleteQueryBuilder<ENTITY>
    fun single(entity: ENTITY): Query<Unit>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): Query<Unit>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): Query<Unit>
}

internal data class EntityDeleteQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val options: EntityDeleteOptions = EntityDeleteOptions.default
) :
    EntityDeleteQueryBuilder<ENTITY> {

    override fun options(configure: (EntityDeleteOptions) -> EntityDeleteOptions): EntityDeleteQueryBuilder<ENTITY> {
        return copy(options = configure(options))
    }

    override fun single(entity: ENTITY): Query<Unit> {
        context.target.checkIdValueNotNull(entity)
        return Query { visitor ->
            visitor.entityDeleteSingleQuery(context, options, entity)
        }
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): Query<Unit> {
        context.target.checkIdValueNotNull(entities)
        return Query { visitor ->
            visitor.entityDeleteBatchQuery(context, options.asEntityBatchDeleteOption(batchSize), entities)
        }
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): Query<Unit> {
        return batch(entities.toList())
    }
}
