package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class EntityUpdateRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
) {
    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = config.dialect.getEntityUpdateStatementBuilder(BuilderDialect(config), context, entity)
        return builder.build()
    }

    internal fun preUpdate(config: DatabaseConfig, entity: ENTITY): ENTITY {
        val clock = config.clockProvider.now()
        return context.target.preUpdate(entity, clock)
    }

    internal fun postUpdate(entity: ENTITY, count: Long, index: Int? = null): ENTITY {
        val metamodel = context.target
        if (metamodel.versionProperty() != null) {
            checkOptimisticLock(context.options, metamodel, entity, count, index)
        } else {
            checkEntityExistence(context.options, metamodel, entity, count, index)
        }
        return if (!context.options.disableOptimisticLock) {
            metamodel.postUpdate(entity)
        } else {
            entity
        }
    }
}
