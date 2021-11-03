package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityInsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityInsertQuery<T> : Query<T> {

    fun options(configure: (EntityInsertOptions) -> EntityInsertOptions): EntityInsertQuery<T>

    data class Single<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        private val context: EntityInsertContext<ENTITY, ID, META>,
        private val options: EntityInsertOptions,
        private val entity: ENTITY
    ) : EntityInsertQuery<ENTITY> {
        override fun options(configure: (EntityInsertOptions) -> EntityInsertOptions): EntityInsertQuery<ENTITY> {
            return copy(options = configure(options))
        }

        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entityInsertSingleQuery(context, options, entity)
        }
    }

    data class Multiple<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        private val context: EntityInsertContext<ENTITY, ID, META>,
        private val options: EntityInsertOptions,
        private val entities: List<ENTITY>
    ) : EntityInsertQuery<List<ENTITY>> {
        override fun options(configure: (EntityInsertOptions) -> EntityInsertOptions): EntityInsertQuery<List<ENTITY>> {
            return copy(options = configure(options))
        }

        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entityInsertMultipleQuery(context, options, entities)
        }
    }

    data class Batch<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        private val context: EntityInsertContext<ENTITY, ID, META>,
        private val options: EntityInsertOptions,
        private val entities: List<ENTITY>,
        private val batchSize: Int?
    ) : EntityInsertQuery<List<ENTITY>> {
        override fun options(configure: (EntityInsertOptions) -> EntityInsertOptions): EntityInsertQuery<List<ENTITY>> {
            return copy(options = configure(options))
        }

        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entityInsertBatchQuery(context, options.asEntityBatchInsertOption(batchSize), entities)
        }
    }
}
