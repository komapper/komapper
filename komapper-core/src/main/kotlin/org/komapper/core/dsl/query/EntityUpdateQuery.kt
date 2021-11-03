package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityUpdateOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityUpdateQuery<T> : Query<T> {

    fun options(configure: (EntityUpdateOptions) -> EntityUpdateOptions): EntityUpdateQuery<T>

    data class Single<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        private val context: EntityUpdateContext<ENTITY, ID, META>,
        private val options: EntityUpdateOptions,
        private val entity: ENTITY
    ) : EntityUpdateQuery<ENTITY> {
        override fun options(configure: (EntityUpdateOptions) -> EntityUpdateOptions): EntityUpdateQuery<ENTITY> {
            return copy(options = configure(options))
        }

        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entityUpdateSingleQuery(context, options, entity)
        }
    }

    data class Batch<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        private val context: EntityUpdateContext<ENTITY, ID, META>,
        private val options: EntityUpdateOptions,
        private val entities: List<ENTITY>,
        private val batchSize: Int?
    ) : EntityUpdateQuery<List<ENTITY>> {
        override fun options(configure: (EntityUpdateOptions) -> EntityUpdateOptions): EntityUpdateQuery<List<ENTITY>> {
            return copy(options = configure(options))
        }

        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entityUpdateBatchQuery(context, options.asEntityBatchUpdateOption(batchSize), entities)
        }
    }
}
