package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

@ThreadSafe
interface EntityInsertQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *, *> = emptyArray()): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *, *> = emptyArray()): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun single(entity: ENTITY): EntityInsertQuery<ENTITY>
    fun multiple(entities: List<ENTITY>): EntityInsertQuery<List<ENTITY>>
    fun multiple(vararg entities: ENTITY): EntityInsertQuery<List<ENTITY>>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityInsertQuery<List<ENTITY>>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityInsertQuery<List<ENTITY>>
    fun select(block: () -> SubqueryExpression<ENTITY>): RelationInsertQuery<ENTITY, ID, META, Pair<Int, List<ID>>>
    fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertQuery<ENTITY, ID, META, Pair<Int, ID?>>
}

internal data class EntityInsertQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
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
        return EntityUpsertQueryBuilderImpl(newContext)
    }

    override fun single(entity: ENTITY): EntityInsertQuery<ENTITY> {
        return EntityInsertSingleQuery(context, entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityInsertQuery<List<ENTITY>> {
        return EntityInsertMultipleQuery(context, entities)
    }

    override fun multiple(vararg entities: ENTITY): EntityInsertQuery<List<ENTITY>> {
        return multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityInsertQuery<List<ENTITY>> {
        val context = if (batchSize != null) {
            context.copy(options = context.options.copy(batchSize = batchSize))
        } else context
        return EntityInsertBatchQuery(context, entities)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityInsertQuery<List<ENTITY>> {
        return batch(entities.toList(), batchSize)
    }

    override fun select(block: () -> SubqueryExpression<ENTITY>): RelationInsertQuery<ENTITY, ID, META, Pair<Int, List<ID>>> {
        val newContext = context.asRelationInsertSelectContext(block)
        return RelationInsertSelectQuery(newContext)
    }

    override fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertQuery<ENTITY, ID, META, Pair<Int, ID?>> {
        val newContext = context.asRelationInsertValuesContext(declaration)
        return RelationInsertValuesQuery(newContext)
    }
}
