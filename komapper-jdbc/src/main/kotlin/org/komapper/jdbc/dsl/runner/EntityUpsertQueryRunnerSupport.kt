package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class EntityUpsertQueryRunnerSupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val options: InsertOptions
) {

    private val support: EntityInsertQueryRunnerSupport<ENTITY, ID, META> =
        EntityInsertQueryRunnerSupport(context.insertContext, options)

    fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preInsert(config, entity)
    }

    fun <T> upsert(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, options)
        return execute(executor)
    }

    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entities)
        return builder.build()
    }
}
