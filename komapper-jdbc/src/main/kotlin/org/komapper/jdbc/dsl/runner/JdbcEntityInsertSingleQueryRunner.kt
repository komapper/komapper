package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityInsertOptions
import org.komapper.core.dsl.runner.EntityInsertSingleQueryRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityInsertSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    options: EntityInsertOptions,
    private val entity: ENTITY
) : JdbcQueryRunner<ENTITY> {

    private val runner: EntityInsertSingleQueryRunner<ENTITY, ID, META> =
        EntityInsertSingleQueryRunner(context, options, entity)

    private val support: JdbcEntityInsertQueryRunnerSupport<ENTITY, ID, META> =
        JdbcEntityInsertQueryRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig): ENTITY {
        val newEntity = preInsert(config)
        val (_, generatedKeys) = insert(config, newEntity)
        return postInsert(newEntity, generatedKeys)
    }

    private fun preInsert(config: JdbcDatabaseConfig): ENTITY {
        return support.preInsert(config, entity)
    }

    private fun insert(config: JdbcDatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = runner.buildStatement(config, entity)
        return support.insert(config) { it.executeUpdate(statement) }
    }

    private fun postInsert(entity: ENTITY, generatedKeys: LongArray): ENTITY {
        val key = generatedKeys.firstOrNull()
        return if (key != null) {
            support.postInsert(entity, key)
        } else {
            entity
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
