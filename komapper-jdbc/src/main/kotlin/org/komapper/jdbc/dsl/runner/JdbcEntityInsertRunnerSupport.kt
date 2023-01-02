package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcEntityInsertRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
) {

    fun preInsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        val newEntity = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.Sequence<ENTITY, ID> ->
                if (!context.target.disableSequenceAssignment() && !context.options.disableSequenceAssignment) {
                    val id = idGenerator.execute(config, context.options)
                    idGenerator.property.setter(entity, id)
                } else {
                    null
                }
            else -> null
        }
        val clock = config.clockProvider.now()
        return context.target.preInsert(newEntity ?: entity, clock)
    }

    fun <T> insert(config: JdbcDatabaseConfig, usesGeneratedKeys: Boolean, execute: (JdbcExecutor) -> T): T {
        val generatedColumn = if (usesGeneratedKeys && context.options.returnGeneratedKeys) {
            context.target.getAutoIncrementProperty()?.columnName
        } else {
            null
        }
        val executor = JdbcExecutor(config, context.options, generatedColumn)
        return execute(executor)
    }
}
