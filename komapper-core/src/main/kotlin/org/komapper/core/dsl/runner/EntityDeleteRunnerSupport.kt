package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class EntityDeleteRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityDeleteContext<ENTITY, ID, META>,
) {
    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = config.dialect.getEntityDeleteStatementBuilder(BuilderDialect(config), context, entity)
        return builder.build()
    }

    internal fun postDelete(entity: ENTITY, count: Long, index: Int? = null) {
        val metamodel = context.target
        if (metamodel.versionProperty() != null) {
            checkOptimisticLock(context.options, metamodel, entity, count, index)
        } else {
            checkEntityExistence(context.options, metamodel, entity, count, index)
        }
    }
}
