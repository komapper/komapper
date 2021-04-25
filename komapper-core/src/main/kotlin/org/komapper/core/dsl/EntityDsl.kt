package org.komapper.core.dsl

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityBatchDeleteQuery
import org.komapper.core.dsl.query.EntityBatchDeleteQueryImpl
import org.komapper.core.dsl.query.EntityBatchInsertQuery
import org.komapper.core.dsl.query.EntityBatchInsertQueryImpl
import org.komapper.core.dsl.query.EntityBatchUpdateQuery
import org.komapper.core.dsl.query.EntityBatchUpdateQueryImpl
import org.komapper.core.dsl.query.EntityDeleteQuery
import org.komapper.core.dsl.query.EntityDeleteQueryImpl
import org.komapper.core.dsl.query.EntityInsertQuery
import org.komapper.core.dsl.query.EntityInsertQueryImpl
import org.komapper.core.dsl.query.EntityMultipleInsertQuery
import org.komapper.core.dsl.query.EntityMultipleInsertQueryImpl
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntityUpdateQuery
import org.komapper.core.dsl.query.EntityUpdateQueryImpl
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.scope.WhereDeclaration

object EntityDsl : Dsl {

    private object Messages {
        const val idValueRequired = "The id value must not be null."
        fun idValueRequired(index: Int) = "The id value must not be null. (index=$index)"
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> first(
        metamodel: META,
        where: WhereDeclaration
    ): Query<ENTITY> {
        return EntitySelectQueryImpl(EntitySelectContext(metamodel))
            .where(where)
            .limit(1)
            .first()
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> firstOrNull(
        metamodel: META,
        where: WhereDeclaration
    ): Query<ENTITY?> {
        return EntitySelectQueryImpl(EntitySelectContext(metamodel))
            .where(where)
            .limit(1)
            .firstOrNull()
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META
    ): EntitySelectQuery<ENTITY> {
        return EntitySelectQueryImpl(EntitySelectContext(metamodel))
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insert(
        metamodel: META,
        entity: ENTITY
    ): EntityInsertQuery<ENTITY, ID, META> {
        return EntityInsertQueryImpl(EntityInsertContext(metamodel), entity)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> update(
        metamodel: META,
        entity: ENTITY
    ): EntityUpdateQuery<ENTITY> {
        require(hasIdValue(metamodel, entity)) { Messages.idValueRequired }
        return EntityUpdateQueryImpl(EntityUpdateContext(metamodel), entity)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> delete(
        metamodel: META,
        entity: ENTITY
    ): EntityDeleteQuery<ENTITY> {
        require(hasIdValue(metamodel, entity)) { Messages.idValueRequired }
        return EntityDeleteQueryImpl(EntityDeleteContext(metamodel), entity)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insertMultiple(
        metamodel: META,
        entities: List<ENTITY>
    ): EntityMultipleInsertQuery<ENTITY, ID, META> {
        return EntityMultipleInsertQueryImpl(EntityInsertContext(metamodel), entities)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insertBatch(
        metamodel: META,
        entities: List<ENTITY>
    ): EntityBatchInsertQuery<ENTITY, ID, META> {
        return EntityBatchInsertQueryImpl(EntityInsertContext(metamodel), entities)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> updateBatch(
        metamodel: META,
        entities: List<ENTITY>
    ): EntityBatchUpdateQuery<ENTITY> {
        for ((i, entity) in entities.withIndex()) {
            require(hasIdValue(metamodel, entity)) { Messages.idValueRequired(i) }
        }
        return EntityBatchUpdateQueryImpl(EntityUpdateContext(metamodel), entities)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> deleteBatch(
        metamodel: META,
        entities: List<ENTITY>
    ): EntityBatchDeleteQuery<ENTITY> {
        for ((i, entity) in entities.withIndex()) {
            require(hasIdValue(metamodel, entity)) { Messages.idValueRequired(i) }
        }
        return EntityBatchDeleteQueryImpl(EntityDeleteContext(metamodel), entities)
    }

    private fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> hasIdValue(
        metamodel: META,
        entity: ENTITY
    ): Boolean {
        val idProperties = metamodel.idProperties()
        return idProperties.isNotEmpty() &&
            idProperties.map { it.getter(entity) }.all { it != null }
    }
}
