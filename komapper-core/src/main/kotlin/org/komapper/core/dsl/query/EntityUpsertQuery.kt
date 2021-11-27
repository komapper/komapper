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
    private val entity: ENTITY
) : EntityUpsertQuery<Int> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<Int> {
        val options = configure(context.insertContext.options)
        val insertContext = context.insertContext.copy(options = options)
        val newContext = context.copy(insertContext = insertContext)
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleQuery(context, entity)
    }
}

data class EntityUpsertMultipleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : EntityUpsertQuery<Int> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<Int> {
        val options = configure(context.insertContext.options)
        val insertContext = context.insertContext.copy(options = options)
        val newContext = context.copy(insertContext = insertContext)
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertMultipleQuery(context, entities)
    }
}

data class EntityUpsertBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertQuery<List<Int>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<List<Int>> {
        val options = configure(context.insertContext.options)
        val insertContext = context.insertContext.copy(options = options)
        val newContext = context.copy(insertContext = insertContext)
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertBatchQuery(context, entities)
    }
}
