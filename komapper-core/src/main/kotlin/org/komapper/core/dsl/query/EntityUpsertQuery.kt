package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityUpsertQuery<T> : Query<T> {
    fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<T>
}

data class EntityUpsertSingleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val options: InsertOptions,
    private val entity: ENTITY
) : EntityUpsertQuery<Int> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<Int> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleQuery(context, options, entity)
    }
}

data class EntityUpsertMultipleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val options: InsertOptions,
    private val entities: List<ENTITY>
) : EntityUpsertQuery<Int> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<Int> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertMultipleQuery(context, options, entities)
    }
}

data class EntityUpsertBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val options: InsertOptions,
    private val entities: List<ENTITY>,
    private val batchSize: Int?
) : EntityUpsertQuery<List<Int>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<List<Int>> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertBatchQuery(context, options.copy(batchSize = batchSize), entities)
    }
}
