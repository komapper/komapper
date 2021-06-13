package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.EntityInsertOptions

@ThreadSafe
interface EntityInsertQueryBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> {
    fun options(configure: (EntityInsertOptions) -> EntityInsertOptions): EntityInsertQueryBuilder<ENTITY, ID, META>
    fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *, *> = emptyArray()): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *, *> = emptyArray()): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun single(entity: ENTITY): Query<ENTITY>
    fun multiple(entities: List<ENTITY>): Query<List<ENTITY>>
    fun multiple(vararg entities: ENTITY): Query<List<ENTITY>>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): Query<List<ENTITY>>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): Query<List<ENTITY>>
}

internal data class EntityInsertQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val options: EntityInsertOptions = EntityInsertOptions.default
) :
    EntityInsertQueryBuilder<ENTITY, ID, META> {

    override fun options(configure: (EntityInsertOptions) -> EntityInsertOptions): EntityInsertQueryBuilderImpl<ENTITY, ID, META> {
        return copy(options = configure(options))
    }

    override fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *, *>): EntityUpsertQueryBuilder<ENTITY, ID, META> {
        return createEntityUpdateBuilder(keys.toList(), DuplicateKeyType.UPDATE)
    }

    override fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *, *>): EntityUpsertQueryBuilder<ENTITY, ID, META> {
        return createEntityUpdateBuilder(keys.toList(), DuplicateKeyType.IGNORE)
    }

    private fun createEntityUpdateBuilder(
        keys: List<PropertyMetamodel<ENTITY, *, *>>,
        duplicateKeyType: DuplicateKeyType
    ): EntityUpsertQueryBuilder<ENTITY, ID, META> {
        val newContext = context.asEntityUpsertContext(keys, duplicateKeyType)
        return EntityUpsertQueryBuilderImpl(newContext, options)
    }

    override fun single(entity: ENTITY): Query<ENTITY> = Query { visitor ->
        visitor.entityInsertSingleQuery(context, options, entity)
    }

    override fun multiple(entities: List<ENTITY>): Query<List<ENTITY>> = Query { visitor ->
        visitor.entityInsertMultipleQuery(context, options, entities)
    }

    override fun multiple(vararg entities: ENTITY): Query<List<ENTITY>> {
        return multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): Query<List<ENTITY>> = Query { visitor ->
        visitor.entityInsertBatchQuery(context, options.asEntityBatchInsertOption(batchSize), entities)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): Query<List<ENTITY>> {
        return batch(entities.toList())
    }
}
