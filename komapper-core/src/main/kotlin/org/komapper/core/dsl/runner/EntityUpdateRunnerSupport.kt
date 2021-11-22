package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.VersionOptions

internal class EntityUpdateRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val options: VersionOptions
) {

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityUpdateStatementBuilder(config.dialect, context, options, entity)
        return builder.build()
    }
}
