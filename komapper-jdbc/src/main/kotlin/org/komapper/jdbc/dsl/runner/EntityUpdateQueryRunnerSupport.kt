package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.VersionOptions
import org.komapper.core.dsl.query.checkOptimisticLock
import org.komapper.jdbc.DatabaseConfig

internal class EntityUpdateQueryRunnerSupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val options: VersionOptions
) {

    fun preUpdate(config: DatabaseConfig, entity: ENTITY): ENTITY {
        val clock = config.clockProvider.now()
        return context.target.preUpdate(entity, clock)
    }

    fun <T> update(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, options)
        return execute(executor)
    }

    fun postUpdate(entity: ENTITY, count: Int, index: Int? = null): ENTITY {
        if (context.target.versionProperty() != null) {
            checkOptimisticLock(options, count, index)
        }
        return if (!options.ignoreVersion) {
            context.target.postUpdate(entity)
        } else {
            entity
        }
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityUpdateStatementBuilder(config.dialect, context, options, entity)
        return builder.build()
    }
}
