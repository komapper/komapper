package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteOptions

@ThreadSafe
interface EntityDeleteQueryBuilder<ENTITY : Any> {
    fun single(entity: ENTITY): EntityDeleteQuery
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityDeleteQuery
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityDeleteQuery
    fun where(declaration: WhereDeclaration): SqlDeleteQuery
    fun all(): SqlDeleteQuery
}

internal data class EntityDeleteQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val options: EntityDeleteOptions = EntityDeleteOptions.default
) :
    EntityDeleteQueryBuilder<ENTITY> {

    override fun single(entity: ENTITY): EntityDeleteQuery {
        context.target.checkIdValueNotNull(entity)
        return EntityDeleteQuery.Single(context, options, entity)
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityDeleteQuery {
        context.target.checkIdValueNotNull(entities)
        return EntityDeleteQuery.Batch(context, options, entities, batchSize)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityDeleteQuery {
        return batch(entities.toList())
    }

    override fun where(declaration: WhereDeclaration): SqlDeleteQuery {
        return asSqlDeleteQuery().where(declaration)
    }

    override fun all(): SqlDeleteQuery {
        return asSqlDeleteQuery()
    }

    private fun asSqlDeleteQuery(): SqlDeleteQuery {
        return SqlDeleteQueryImpl(context.asSqlDeleteContext(), options.asSqlDeleteOptions())
    }
}
