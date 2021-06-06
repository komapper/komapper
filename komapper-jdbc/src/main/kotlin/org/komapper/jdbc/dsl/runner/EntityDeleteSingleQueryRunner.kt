package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityDeleteOption
import org.komapper.jdbc.DatabaseConfig

internal data class EntityDeleteSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val option: EntityDeleteOption
) : JdbcQueryRunner<Unit> {

    private val support: EntityDeleteQuerySupport<ENTITY, ID, META> = EntityDeleteQuerySupport(context, option)

    override fun run(config: DatabaseConfig) {
        val (count) = delete(config)
        postDelete(count)
    }

    private fun delete(config: DatabaseConfig): Pair<Int, LongArray> {
        val statement = buildStatement(config)
        return support.delete(config) { it.executeUpdate(statement) }
    }

    private fun postDelete(count: Int) {
        support.postDelete(count)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        return support.buildStatement(config, entity)
    }
}
