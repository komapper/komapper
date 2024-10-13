package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcRelationInsertValuesSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) {
    fun <R> handleIdGenerator(config: JdbcDatabaseConfig, callback: Callback<ENTITY, ID, R>): R {
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
        fun autoIncrement(idGenerator: IdGenerator.AutoIncrement<ENTITY, ID>): R
        fun sequence(idGenerator: IdGenerator.Sequence<ENTITY, ID>, id: ID): R
        fun other(): R
    }
}
