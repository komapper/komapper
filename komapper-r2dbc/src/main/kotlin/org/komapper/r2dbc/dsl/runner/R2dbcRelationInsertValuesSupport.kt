package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcRelationInsertValuesSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) {

    suspend fun <R> handleIdGenerator(config: R2dbcDatabaseConfig, callback: Callback<ENTITY, ID, R>): R {
        return when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.AutoIncrement<ENTITY, ID> -> {
                callback.autoIncrement(idGenerator)
            }

            is IdGenerator.Sequence<ENTITY, ID> -> {
                if (context.target.disableSequenceAssignment() || context.options.disableSequenceAssignment) {
                    callback.other()
                } else {
                    val id = idGenerator.execute(config, context.options)
                    callback.sequence(idGenerator, id)
                }
            }

            else -> {
                callback.other()
            }
        }
    }

    internal interface Callback<ENTITY : Any, ID : Any, R> {
        suspend fun autoIncrement(idGenerator: IdGenerator.AutoIncrement<ENTITY, ID>): R
        suspend fun sequence(idGenerator: IdGenerator.Sequence<ENTITY, ID>, id: ID): R
        suspend fun other(): R
    }
}
