package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.UpsertAssignmentDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.scope.AssignmentScope

@ThreadSafe
interface EntityUpsertQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun set(declaration: AssignmentScope<ENTITY>.(META) -> Unit): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun single(entity: ENTITY): EntityUpsertQuery<Int>
    fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Int>
    fun multiple(vararg entities: ENTITY): EntityUpsertQuery<Int>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityUpsertQuery<List<Int>>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityUpsertQuery<List<Int>>
}

internal data class EntityUpsertQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val options: InsertOptions
) : EntityUpsertQueryBuilder<ENTITY, ID, META> {

    override fun set(declaration: UpsertAssignmentDeclaration<ENTITY, META>): EntityUpsertQueryBuilder<ENTITY, ID, META> {
        val newContext = context.copy(set = context.set + declaration)
        return copy(context = newContext)
    }

    override fun single(entity: ENTITY): EntityUpsertQuery<Int> {
        return EntityUpsertSingleQuery(context, options, entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Int> {
        return EntityUpsertMultipleQuery(context, options, entities)
    }

    override fun multiple(vararg entities: ENTITY): EntityUpsertQuery<Int> {
        return multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Int>> {
        return EntityUpsertBatchQuery(context, options, entities, batchSize)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityUpsertQuery<List<Int>> {
        return batch(entities.toList(), batchSize)
    }
}
