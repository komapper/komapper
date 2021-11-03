package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityDeleteQuery : Query<Unit> {

    fun options(configure: (EntityDeleteOptions) -> EntityDeleteOptions): EntityDeleteQuery

    data class Single<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        private val context: EntityDeleteContext<ENTITY, ID, META>,
        private val options: EntityDeleteOptions,
        private val entity: ENTITY
    ) : EntityDeleteQuery {

        override fun options(configure: (EntityDeleteOptions) -> EntityDeleteOptions): EntityDeleteQuery {
            return copy(options = configure(options))
        }

        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entityDeleteSingleQuery(context, options, entity)
        }
    }

    data class Batch<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        private val context: EntityDeleteContext<ENTITY, ID, META>,
        private val options: EntityDeleteOptions,
        private val entities: List<ENTITY>,
        private val batchSize: Int?
    ) : EntityDeleteQuery {

        override fun options(configure: (EntityDeleteOptions) -> EntityDeleteOptions): EntityDeleteQuery {
            return copy(options = configure(options))
        }

        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entityDeleteBatchQuery(context, options.asEntityBatchDeleteOption(batchSize), entities)
        }
    }
}
