package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.jdbc.JdbcExecutor

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

    fun buildStatement(dialect: Dialect, entity: ENTITY): Statement {
        val builder = EntityDeleteStatementBuilder(dialect, context, entity, option)
        return builder.build()
    }
}
