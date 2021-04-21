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
import org.komapper.core.dsl.query.EntityMultiInsertQuery
import org.komapper.core.dsl.query.EntityMultiInsertQueryImpl
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntityUpdateQuery
import org.komapper.core.dsl.query.EntityUpdateQueryImpl
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.scope.WhereDeclaration

object EntityQuery : Dsl {

    private object Messages {
        const val idValueRequired = "The id value must not be null."
        fun idValueRequired(index: Int) = "The id value must not be null. (index=$index)"
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> first(
        entityMetamodel: META,
        declaration: WhereDeclaration
    ): Query<ENTITY> {
        return EntitySelectQueryImpl(EntitySelectContext(entityMetamodel))
            .where(declaration)
            .limit(1)
            .first()
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> firstOrNull(
        entityMetamodel: META,
        declaration: WhereDeclaration
    ): Query<ENTITY?> {
        return EntitySelectQueryImpl(EntitySelectContext(entityMetamodel))
            .where(declaration)
            .limit(1)
            .firstOrNull()
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> from(
        entityMetamodel: META
    ): EntitySelectQuery<ENTITY> {
        return EntitySelectQueryImpl(EntitySelectContext(entityMetamodel))
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insert(
        entityMetamodel: META,
        entity: ENTITY
    ): EntityInsertQuery<ENTITY, ID, META> {
        return EntityInsertQueryImpl(EntityInsertContext(entityMetamodel), entity)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> update(
        entityMetamodel: META,
        entity: ENTITY
    ): EntityUpdateQuery<ENTITY> {
        require(hasIdValue(entityMetamodel, entity)) { Messages.idValueRequired }
        return EntityUpdateQueryImpl(EntityUpdateContext(entityMetamodel), entity)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> delete(
        entityMetamodel: META,
        entity: ENTITY
    ): EntityDeleteQuery<ENTITY> {
        require(hasIdValue(entityMetamodel, entity)) { Messages.idValueRequired }
        return EntityDeleteQueryImpl(EntityDeleteContext(entityMetamodel), entity)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insertMulti(
        entityMetamodel: META,
        entities: List<ENTITY>
    ): EntityMultiInsertQuery<ENTITY, ID, META> {
        return EntityMultiInsertQueryImpl(EntityInsertContext(entityMetamodel), entities)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insertBatch(
        entityMetamodel: META,
        entities: List<ENTITY>
    ): EntityBatchInsertQuery<ENTITY, ID, META> {
        return EntityBatchInsertQueryImpl(EntityInsertContext(entityMetamodel), entities)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> updateBatch(
        entityMetamodel: META,
        entities: List<ENTITY>
    ): EntityBatchUpdateQuery<ENTITY> {
        for ((i, entity) in entities.withIndex()) {
            require(hasIdValue(entityMetamodel, entity)) { Messages.idValueRequired(i) }
        }
        return EntityBatchUpdateQueryImpl(EntityUpdateContext(entityMetamodel), entities)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> deleteBatch(
        entityMetamodel: META,
        entities: List<ENTITY>
    ): EntityBatchDeleteQuery<ENTITY> {
        for ((i, entity) in entities.withIndex()) {
            require(hasIdValue(entityMetamodel, entity)) { Messages.idValueRequired(i) }
        }
        return EntityBatchDeleteQueryImpl(EntityDeleteContext(entityMetamodel), entities)
    }

    private fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> hasIdValue(
        entityMetamodel: META,
        entity: ENTITY
    ): Boolean {
        val idProperties = entityMetamodel.idProperties()
        return idProperties.isNotEmpty() &&
            idProperties.map { it.getter(entity) }.all { it != null }
    }
}
