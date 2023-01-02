package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcEntityInsertRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
) {

    suspend fun preInsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        val newEntity = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.Sequence<ENTITY, ID> -> {
                if (!context.target.disableSequenceAssignment() && !context.options.disableSequenceAssignment) {
                    val id = idGenerator.execute(config, context.options)
                    idGenerator.property.setter(entity, id)
                } else {
                    null
                }
            }
            else -> null
        }
        val clock = config.clockProvider.now()
        return context.target.preInsert(newEntity ?: entity, clock)
    }

    suspend fun <T> insert(config: R2dbcDatabaseConfig, usesGeneratedKeys: Boolean, execute: suspend (R2dbcExecutor) -> T): T {
        val generatedColumn = if (usesGeneratedKeys && context.options.returnGeneratedKeys) {
            context.target.getAutoIncrementProperty()?.columnName
        } else {
            null
        }
        val executor = R2dbcExecutor(config, context.options, generatedColumn)
        return execute(executor)
    }
}
