package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.OptimisticLockOptions

internal class EntityDeleteRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityDeleteContext<ENTITY, ID, META>,
    val options: OptimisticLockOptions
) {

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityDeleteStatementBuilder(config.dialect, context, options, entity)
        return builder.build()
    }
}
