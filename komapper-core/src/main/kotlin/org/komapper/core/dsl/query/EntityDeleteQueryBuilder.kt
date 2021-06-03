package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityDeleteOption

@ThreadSafe
interface EntityDeleteQueryBuilder<ENTITY : Any> {
    fun option(configure: (EntityDeleteOption) -> EntityDeleteOption): EntityDeleteQueryBuilder<ENTITY>
    fun single(entity: ENTITY): Query<Unit>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): Query<Unit>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): Query<Unit>
}

internal data class EntityDeleteQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val option: EntityDeleteOption = EntityDeleteOption.default
) :
    EntityDeleteQueryBuilder<ENTITY> {

    override fun option(configure: (EntityDeleteOption) -> EntityDeleteOption): EntityDeleteQueryBuilderImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override fun single(entity: ENTITY): Query<Unit> {
        context.target.checkIdValueNotNull(entity)
        return EntityDeleteSingleQuery(context, entity, option)
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): Query<Unit> {
        context.target.checkIdValueNotNull(entities)
        return EntityDeleteBatchQuery(context, entities, option.asEntityBatchDeleteOption(batchSize))
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): Query<Unit> {
        return batch(entities.toList())
    }
}
