package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.VersionOption

internal class EntityUpdateQuerySupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val option: VersionOption
) {

    fun preUpdate(config: DatabaseConfig, entity: ENTITY): ENTITY {
        val clock = config.clockProvider.now()
        return context.target.preUpdate(entity, clock)
    }

    fun <T> update(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, option)
        return execute(executor)
    }

    fun postUpdate(entity: ENTITY, count: Int, index: Int? = null): ENTITY {
        if (context.target.versionProperty() != null) {
            checkOptimisticLock(option, count, index)
        }
        return if (!option.ignoreVersion) {
            context.target.postUpdate(entity)
        } else {
            entity
        }
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityUpdateStatementBuilder(config.dialect, context, option, entity)
        return builder.build()
    }
}
