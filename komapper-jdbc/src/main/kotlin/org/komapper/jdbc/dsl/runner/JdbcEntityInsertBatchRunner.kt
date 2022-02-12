package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityInsertBatchRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityInsertBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : JdbcRunner<List<ENTITY>> {

    private val runner: EntityInsertBatchRunner<ENTITY, ID, META> =
        EntityInsertBatchRunner(context, entities)

    private val support: JdbcEntityInsertRunnerSupport<ENTITY, ID, META> =
        JdbcEntityInsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        val batchResults = insert(config, newEntities)
        return postInsert(newEntities, batchResults.map { it.second })
    }

    private fun preInsert(config: JdbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private fun insert(config: JdbcDatabaseConfig, entities: List<ENTITY>): List<Pair<Int, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.insert(config, true) { it.executeBatch(statements) }
    }

    private fun postInsert(entities: List<ENTITY>, generatedKeys: List<Long?>): List<ENTITY> {
        return runner.postInsert(entities, generatedKeys)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
