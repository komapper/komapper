package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

/**
 * The builder of insert queries.
 * @param ENTITY the entity type
 * @param ID the entity id type
 * @param META the entity metamodel type
 */
// TODO rename
@ThreadSafe
interface EntityInsertQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *, *> = emptyArray()): EntityInsertOnDuplicateKeyUpdateQuery<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *, *> = emptyArray()): EntityInsertOnDuplicateKeyIgnoreQuery<ENTITY, ID, META>

    /**
     * Builds a query to insert a single entity.
     * @param entity the entity to be inserted
     * @return the query
     */
    fun single(entity: ENTITY): EntityInsertQuery<ENTITY>

    /**
     * Builds a query to bulk insert a list of entities.
     * @param entities the entities to be inserted
     * @return the query
     */
    fun multiple(entities: List<ENTITY>): EntityInsertQuery<List<ENTITY>>

    /**
     * Builds a query to bulk insert an array of entities.
     * @param entities the entities to be inserted
     * @return the query
     */
    fun multiple(vararg entities: ENTITY): EntityInsertQuery<List<ENTITY>>

    /**
     * Builds a query to insert a list of entities in a batch.
     * @param entities the entities to be inserted
     * @return the query
     */
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityInsertQuery<List<ENTITY>>

    /**
     * Builds a query to insert an array of entities in a batch.
     * @param entities the entities to be inserted
     * @return the query
     */
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityInsertQuery<List<ENTITY>>

    /**
     * Builds a query to insert rows using the select expression.
     * @param block the select expression
     * @return the query
     */
    fun select(block: () -> SubqueryExpression<ENTITY>): RelationInsertQuery<ENTITY, ID, META, Pair<Int, List<ID>>>

    /**
     * Builds a query to insert a single row with specified values.
     * @param declaration the assignment declaration
     * @return the query
     */
    fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertQuery<ENTITY, ID, META, Pair<Int, ID?>>
}

internal data class EntityInsertQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
) :
    EntityInsertQueryBuilder<ENTITY, ID, META> {

    override fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *, *>): EntityInsertOnDuplicateKeyUpdateQuery<ENTITY, ID, META> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.UPDATE)
        return EntityInsertOnDuplicateKeyUpdateQueryImpl(newContext)
    }

    override fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *, *>): EntityInsertOnDuplicateKeyIgnoreQuery<ENTITY, ID, META> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.IGNORE)
        return EntityInsertOnDuplicateKeyIgnoreQueryImpl(newContext)
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
