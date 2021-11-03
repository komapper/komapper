package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.declaration.SetDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.EntityUpdateOptions
import org.komapper.core.dsl.visitor.QueryVisitor

@ThreadSafe
interface EntityUpdateQueryBuilder<ENTITY : Any> {
    fun options(configure: (EntityUpdateOptions) -> EntityUpdateOptions): EntityUpdateQueryBuilder<ENTITY>
    fun include(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY>
    fun exclude(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY>
    fun single(entity: ENTITY): Query<ENTITY>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): Query<List<ENTITY>>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): Query<List<ENTITY>>
    fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQuery<ENTITY>
}

internal data class EntityUpdateQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val options: EntityUpdateOptions = EntityUpdateOptions.default
) :
    EntityUpdateQueryBuilder<ENTITY> {

    override fun options(configure: (EntityUpdateOptions) -> EntityUpdateOptions): EntityUpdateQueryBuilder<ENTITY> {
        return copy(options = configure(options))
    }

    override fun include(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY> {
        val newContext = context.copy(includedProperties = properties.toList()).also {
            checkContext(it)
        }
        return copy(context = newContext)
    }

    override fun exclude(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY> {
        val newContext = context.copy(excludedProperties = properties.toList()).also {
            checkContext(it)
        }
        return copy(context = newContext)
    }

    private fun checkContext(context: EntityUpdateContext<ENTITY, ID, META>) {
        if (context.getTargetProperties().isEmpty()) {
            error(
                "Illegal SQL will be generated. The set clause is empty. " +
                    "Include or exclude appropriate properties."
            )
        }
    }

    override fun single(entity: ENTITY): Query<ENTITY> {
        context.target.checkIdValueNotNull(entity)
        return object : Query<ENTITY> {
            override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
                return visitor.entityUpdateSingleQuery(context, options, entity)
            }
        }
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): Query<List<ENTITY>> {
        context.target.checkIdValueNotNull(entities)
        return object : Query<List<ENTITY>> {
            override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
                return visitor.entityUpdateBatchQuery(context, options.asEntityBatchUpdateOption(batchSize), entities)
            }
        }
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): Query<List<ENTITY>> {
        return batch(entities.toList())
    }

    override fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQuery<ENTITY> {
        return asSqlUpdateQueryBuilder().set(declaration)
    }

    private fun asSqlUpdateQueryBuilder(): SqlUpdateQueryBuilder<ENTITY> {
        val query = SqlUpdateQueryImpl(context.asSqlUpdateContext(), options.asSqlUpdateOptions())
        return SqlUpdateQueryBuilderImpl(query)
    }
}
