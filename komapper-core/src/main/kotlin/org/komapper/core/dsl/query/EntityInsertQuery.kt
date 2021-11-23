package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntityInsertQuery<T> : Query<T> {
    fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<T>
}

internal data class EntityInsertSingleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val options: InsertOptions,
    private val entity: ENTITY
) : EntityInsertQuery<ENTITY> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertSingleQuery(context, options, entity)
    }
}

internal data class EntityInsertMultipleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val options: InsertOptions,
    private val entities: List<ENTITY>
) : EntityInsertQuery<List<ENTITY>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<List<ENTITY>> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertMultipleQuery(context, options, entities)
    }
}

internal data class EntityInsertBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val options: InsertOptions,
    private val entities: List<ENTITY>,
) : EntityInsertQuery<List<ENTITY>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<List<ENTITY>> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertBatchQuery(context, options, entities)
    }
}
