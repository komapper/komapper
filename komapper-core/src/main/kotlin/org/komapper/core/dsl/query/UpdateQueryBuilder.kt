package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.DeleteOptions

/**
 * The builder of update queries.
 * @param ENTITY the entity type
 * @param ID the entity id type
 * @param META the entity metamodel type
 */
@ThreadSafe
interface UpdateQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    /**
     * Returns a new builder that updates specified properties.
     * @param properties the properties to be included
     * @return the query
     */
    fun include(vararg properties: PropertyMetamodel<ENTITY, *, *>): UpdateQueryBuilder<ENTITY, ID, META>
    /**
     * Returns a new builder that does not update specified properties.
     * @param properties the properties to be excluded
     * @return the query
     */
    fun exclude(vararg properties: PropertyMetamodel<ENTITY, *, *>): UpdateQueryBuilder<ENTITY, ID, META>
    /**
     * Builds a query to update a single entity.
     * @param entity the entity to be updated
     * @return the query
     */
    fun single(entity: ENTITY): EntityUpdateQuery<ENTITY>
    /**
     * Builds a query to update a list of entities in a batch.
     * @param entities the entities to be updated
     * @param batchSize the batch size. If it is null, the value of [DeleteOptions.batchSize] will be used.
     * @return the query
     */
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityUpdateQuery<List<ENTITY>>
    /**
     * Builds a query to delete an array of entities in a batch.
     * @param entities the entities to be deleted
     * @param batchSize the batch size. If it is null, the value of [DeleteOptions.batchSize] will be used.
     * @return the query
     */
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityUpdateQuery<List<ENTITY>>
    /**
     * Sets the values to be updated.
     * @param declaration the assignment declaration
     * @return the query
     */
    fun set(declaration: AssignmentDeclaration<ENTITY, META>): RelationUpdateQuery<ENTITY, ID, META>
}

internal data class UpdateQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
) :
    UpdateQueryBuilder<ENTITY, ID, META> {

    override fun include(vararg properties: PropertyMetamodel<ENTITY, *, *>): UpdateQueryBuilder<ENTITY, ID, META> {
        val newContext = context.copy(includedProperties = properties.toList()).also {
            checkContext(it)
        }
        return copy(context = newContext)
    }

    override fun exclude(vararg properties: PropertyMetamodel<ENTITY, *, *>): UpdateQueryBuilder<ENTITY, ID, META> {
        val newContext = context.copy(excludedProperties = properties.toList()).also {
            checkContext(it)
        }
        return copy(context = newContext)
    }

    private fun checkContext(context: EntityUpdateContext<ENTITY, ID, META>) {
        if (context.getTargetProperties().isEmpty()) {
            error(
                "Illegal SQL will be generated. The set clause is empty. " +
                    "Include or exclude appropriate properties."
            )
        }
    }

    override fun single(entity: ENTITY): EntityUpdateQuery<ENTITY> {
        context.target.checkIdValueNotNull(entity)
        return EntityUpdateSingleQuery(context, entity)
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpdateQuery<List<ENTITY>> {
        context.target.checkIdValueNotNull(entities)
        val context = if (batchSize != null) {
            context.copy(options = context.options.copy(batchSize = batchSize))
        } else context
        return EntityUpdateBatchQuery(context, entities)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityUpdateQuery<List<ENTITY>> {
        return batch(entities.toList(), batchSize)
    }

    override fun set(declaration: AssignmentDeclaration<ENTITY, META>): RelationUpdateQuery<ENTITY, ID, META> {
        val newContext = context.asRelationUpdateContext(declaration)
        return RelationUpdateQueryImpl(newContext)
    }
}
