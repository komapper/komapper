package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext

internal class EntityDeleteQuerySupport<ENTITY : Any>(
    val context: EntityDeleteContext<ENTITY>,
    val option: EntityDeleteOption
) {

    fun <T> delete(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, option.asJdbcOption())
        return execute(executor)
    }

    fun postDelete(count: Int, index: Int? = null) {
        if (context.entityMetamodel.versionProperty() != null) {
            checkOptimisticLock(option, count, index)
        }
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityDeleteStatementBuilder(config.dialect, context, entity, option)
        return builder.build()
    }
}
