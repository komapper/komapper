package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.declaration.ValuesDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.EntityInsertOptions

@ThreadSafe
interface EntityInsertQueryBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> {
    fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *, *> = emptyArray()): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *, *> = emptyArray()): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun single(entity: ENTITY): EntityInsertQuery<ENTITY>
    fun multiple(entities: List<ENTITY>): EntityInsertQuery<List<ENTITY>>
    fun multiple(vararg entities: ENTITY): EntityInsertQuery<List<ENTITY>>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityInsertQuery<List<ENTITY>>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityInsertQuery<List<ENTITY>>
    fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQuery<ENTITY, ID>
    fun select(block: () -> SubqueryExpression<ENTITY>): SqlInsertQuery<ENTITY, ID>
}

internal data class EntityInsertQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val options: EntityInsertOptions = EntityInsertOptions.default
) :
    EntityInsertQueryBuilder<ENTITY, ID, META> {

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

    override fun single(entity: ENTITY): EntityInsertQuery<ENTITY> {
        return EntityInsertSingleQuery(context, options, entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityInsertQuery<List<ENTITY>> {
        return EntityInsertMultipleQuery(context, options, entities)
    }

    override fun multiple(vararg entities: ENTITY): EntityInsertQuery<List<ENTITY>> {
        return multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityInsertQuery<List<ENTITY>> {
        return EntityInsertBatchQuery(context, options, entities, batchSize)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityInsertQuery<List<ENTITY>> {
        return batch(entities.toList())
    }

    override fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQuery<ENTITY, ID> {
        return asSqlInsertQueryBuilder().values(declaration)
    }

    override fun select(block: () -> SubqueryExpression<ENTITY>): SqlInsertQuery<ENTITY, ID> {
        return asSqlInsertQueryBuilder().select(block)
    }

    private fun asSqlInsertQueryBuilder(): SqlInsertQueryBuilder<ENTITY, ID> {
        val query = SqlInsertQueryImpl(context.asSqlInsertContext(), options.asSqlInsertOptions())
        return SqlInsertQueryBuilderImpl(query)
    }
}
