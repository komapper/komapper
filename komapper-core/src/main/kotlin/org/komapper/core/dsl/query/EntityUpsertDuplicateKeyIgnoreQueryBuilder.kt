package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.scope.AssignmentScope

@ThreadSafe
interface EntityUpsertDuplicateKeyIgnoreQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun set(declaration: AssignmentScope<ENTITY>.(META) -> Unit): EntityUpsertDuplicateKeyIgnoreQueryBuilder<ENTITY, ID, META>
    fun single(entity: ENTITY): EntityUpsertQuery<ENTITY?>
    fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Int>
    fun multiple(vararg entities: ENTITY): EntityUpsertQuery<Int>
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityUpsertQuery<List<Int>>
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityUpsertQuery<List<Int>>
}

internal data class EntityUpsertDuplicateKeyIgnoreQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
) : EntityUpsertDuplicateKeyIgnoreQueryBuilder<ENTITY, ID, META> {

    private val builder: EntityUpsertQueryBuilder<ENTITY, ID, META> = EntityUpsertQueryBuilderImpl(context)

    override fun set(declaration: AssignmentDeclaration<ENTITY, META>): EntityUpsertDuplicateKeyIgnoreQueryBuilder<ENTITY, ID, META> {
        val newContext = context.copy(set = context.set + declaration)
        return copy(context = newContext)
    }

    override fun single(entity: ENTITY): EntityUpsertQuery<ENTITY?> {
        return EntityUpsertDuplicateKeyIgnoreSingleQuery(context, entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Int> {
        return builder.multiple(entities)
    }

    override fun multiple(vararg entities: ENTITY): EntityUpsertQuery<Int> {
        return builder.multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Int>> {
        return builder.batch(entities, batchSize)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityUpsertQuery<List<Int>> {
        return builder.batch(entities.toList(), batchSize)
    }
}
