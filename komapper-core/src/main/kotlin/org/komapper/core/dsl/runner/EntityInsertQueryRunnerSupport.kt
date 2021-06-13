package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions

internal class EntityInsertQueryRunnerSupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    val options: InsertOptions
) {
    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = EntityInsertStatementBuilder(config.dialect, context, entities)
        return builder.build()
    }
}
