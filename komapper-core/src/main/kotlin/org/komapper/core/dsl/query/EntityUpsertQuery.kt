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
        return copy(context = context.copy(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleQuery(context, entity)
    }
}

data class EntityUpsertSingleUpdateQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY
) : Query<ENTITY> {

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleUpdateQuery(context, entity)
    }
}

data class EntityUpsertSingleIgnoreQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY
) : EntityUpsertQuery<ENTITY?> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<ENTITY?> {
        return copy(context = context.copy(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleIgnoreQuery(context, entity)
    }
}

data class EntityUpsertMultipleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : EntityUpsertQuery<Int> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<Int> {
        return copy(context = context.copy(configure))
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
        return copy(context = context.copy(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertBatchQuery(context, entities)
    }
}

private fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityUpsertContext<ENTITY, ID, META>.copy(
    configure: (InsertOptions) -> InsertOptions
): EntityUpsertContext<ENTITY, ID, META> {
    val options = configure(this.insertContext.options)
    val insertContext = this.insertContext.copy(options = options)
    return this.copy(insertContext = insertContext)
}
