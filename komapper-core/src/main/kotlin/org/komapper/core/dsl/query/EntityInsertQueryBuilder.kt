package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityInsertOption

interface EntityInsertQueryBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> {
    fun option(configure: (EntityInsertOption) -> EntityInsertOption): EntityInsertQueryBuilder<ENTITY, ID, META>
    fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun single(entity: ENTITY): Query<ENTITY>
    fun multiple(entities: List<ENTITY>): Query<List<ENTITY>>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): Query<List<ENTITY>>
}

internal data class EntityInsertQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val option: EntityInsertOption = EntityInsertOption.default
) :
    EntityInsertQueryBuilder<ENTITY, ID, META> {

    override fun option(configure: (EntityInsertOption) -> EntityInsertOption): EntityInsertQueryBuilderImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *>): EntityUpsertQueryBuilder<ENTITY, ID, META> {
        return createEntityUpdateBuilder(keys.toList(), DuplicateKeyType.UPDATE)
    }

    override fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *>): EntityUpsertQueryBuilder<ENTITY, ID, META> {
        return createEntityUpdateBuilder(keys.toList(), DuplicateKeyType.IGNORE)
    }

    private fun createEntityUpdateBuilder(
        keys: List<PropertyMetamodel<ENTITY, *>>,
        duplicateKeyType: DuplicateKeyType
    ): EntityUpsertQueryBuilder<ENTITY, ID, META> {
        val newContext = context.asEntityUpsertContext(keys, duplicateKeyType)
        return EntityUpsertQueryBuilderImpl(newContext, option)
    }

    override fun single(entity: ENTITY): Query<ENTITY> {
        return EntityInsertSingleQuery(context, entity, option)
    }

    override fun multiple(entities: List<ENTITY>): Query<List<ENTITY>> {
        return EntityInsertMultipleQuery(context, entities, option)
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): Query<List<ENTITY>> {
        return EntityInsertBatchQuery(context, entities, option.asEntityBatchInsertOption(batchSize))
    }
}
