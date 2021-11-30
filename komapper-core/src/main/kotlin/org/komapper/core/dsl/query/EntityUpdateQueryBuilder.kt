package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

@ThreadSafe
interface EntityUpdateQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun include(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY, ID, META>
    fun exclude(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY, ID, META>
    fun single(entity: ENTITY): EntityUpdateQuery<ENTITY>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityUpdateQuery<List<ENTITY>>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityUpdateQuery<List<ENTITY>>
    fun set(declaration: AssignmentDeclaration<ENTITY, META>): RelationUpdateQuery<ENTITY, ID, META>
}

internal data class EntityUpdateQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
) :
    EntityUpdateQueryBuilder<ENTITY, ID, META> {

    override fun include(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY, ID, META> {
        val newContext = context.copy(includedProperties = properties.toList()).also {
            checkContext(it)
        }
        return copy(context = newContext)
    }

    override fun exclude(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY, ID, META> {
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
