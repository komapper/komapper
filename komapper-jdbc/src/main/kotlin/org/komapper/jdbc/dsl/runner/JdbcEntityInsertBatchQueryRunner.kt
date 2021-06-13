package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityInsertBatchOptions
import org.komapper.core.dsl.runner.EntityInsertBatchQueryRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityInsertBatchQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    options: EntityInsertBatchOptions,
    private val entities: List<ENTITY>
) : JdbcQueryRunner<List<ENTITY>> {

    private val runner: EntityInsertBatchQueryRunner<ENTITY, ID, META> =
        EntityInsertBatchQueryRunner(context, options, entities)

    private val support: JdbcEntityInsertQueryRunnerSupport<ENTITY, ID, META> =
        JdbcEntityInsertQueryRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        val generatedKeys = insert(config, newEntities)
        return postInsert(newEntities, generatedKeys)
    }

    private fun preInsert(config: JdbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private fun insert(config: JdbcDatabaseConfig, entities: List<ENTITY>): LongArray {
        val statements = entities.map { runner.buildStatement(config, it) }
        val (_, keys) = support.insert(config) { it.executeBatch(statements) }
        return keys
    }

    private fun postInsert(entities: List<ENTITY>, generatedKeys: LongArray): List<ENTITY> {
        val iterator = generatedKeys.iterator()
        return entities.map {
            if (iterator.hasNext()) {
                support.postInsert(it, iterator.nextLong())
            } else {
                it
            }
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
