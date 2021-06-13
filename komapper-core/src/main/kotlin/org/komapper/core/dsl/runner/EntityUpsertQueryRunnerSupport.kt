package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions

internal class EntityUpsertQueryRunnerSupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val options: InsertOptions
) {

    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entities)
        return builder.build()
    }
}
