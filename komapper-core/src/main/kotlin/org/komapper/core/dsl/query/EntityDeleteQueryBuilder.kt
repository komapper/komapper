package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

@ThreadSafe
interface EntityDeleteQueryBuilder<ENTITY : Any> {
    fun options(configure: (EntityDeleteOptions) -> EntityDeleteOptions): EntityDeleteQueryBuilder<ENTITY>
    fun single(entity: ENTITY): Query<Unit>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): Query<Unit>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): Query<Unit>
    fun where(declaration: WhereDeclaration): SqlDeleteQuery
    fun all(): SqlDeleteQuery
}

internal data class EntityDeleteQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val options: EntityDeleteOptions = EntityDeleteOptions.default
) :
    EntityDeleteQueryBuilder<ENTITY> {

    override fun options(configure: (EntityDeleteOptions) -> EntityDeleteOptions): EntityDeleteQueryBuilder<ENTITY> {
        return copy(options = configure(options))
    }

    override fun single(entity: ENTITY): Query<Unit> {
        context.target.checkIdValueNotNull(entity)
        return object : Query<Unit> {
            override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
                return visitor.entityDeleteSingleQuery(context, options, entity)
            }
        }
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): Query<Unit> {
        context.target.checkIdValueNotNull(entities)
        return object : Query<Unit> {
            override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
                return visitor.entityDeleteBatchQuery(context, options.asEntityBatchDeleteOption(batchSize), entities)
            }
        }
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): Query<Unit> {
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
