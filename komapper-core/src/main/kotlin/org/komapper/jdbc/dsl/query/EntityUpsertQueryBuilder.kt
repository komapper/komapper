package org.komapper.jdbc.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityInsertOption
import org.komapper.core.dsl.scope.SetScope

@ThreadSafe
interface EntityUpsertQueryBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> {
    fun set(declaration: SetScope<ENTITY>.(META) -> Unit): EntityUpsertQueryBuilder<ENTITY, ID, META>
    fun single(entity: ENTITY): Query<Int>
    fun multiple(entities: List<ENTITY>): Query<Int>
    fun multiple(vararg entities: ENTITY): Query<Int>
    fun batch(entities: List<ENTITY>): Query<List<Int>>
    fun batch(vararg entities: ENTITY): Query<List<Int>>
}

internal data class EntityUpsertQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val option: EntityInsertOption
) : EntityUpsertQueryBuilder<ENTITY, ID, META> {

    override fun set(declaration: SetScope<ENTITY>.(META) -> Unit): EntityUpsertQueryBuilderImpl<ENTITY, ID, META> {
        val scope = SetScope<ENTITY>()
        declaration(scope, context.excluded)
        val newContext = context.copy(assignmentMap = scope.toMap())
        return copy(context = newContext)
    }

    override fun single(entity: ENTITY): Query<Int> {
        return EntityUpsertSingleQuery(context, option, entity)
    }

    override fun multiple(entities: List<ENTITY>): Query<Int> {
        return EntityUpsertMultipleQuery(context, option, entities)
    }

    override fun multiple(vararg entities: ENTITY): Query<Int> {
        return multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>): Query<List<Int>> {
        return EntityUpsertBatchQuery(context, entities, option)
    }

    override fun batch(vararg entities: ENTITY): Query<List<Int>> {
        return batch(entities.toList())
    }
}
