package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityInsertMultipleRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityInsertMultipleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) :
    JdbcRunner<List<ENTITY>> {

    private val runner: EntityInsertMultipleRunner<ENTITY, ID, META> =
        EntityInsertMultipleRunner(context, entities)

    private val support: JdbcEntityInsertRunnerSupport<ENTITY, ID, META> =
        JdbcEntityInsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        val generatedKeys = insert(config, newEntities)
        return postInsert(newEntities, generatedKeys)
    }

    private fun preInsert(config: JdbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private fun insert(config: JdbcDatabaseConfig, entities: List<ENTITY>): List<Long> {
        val statement = runner.buildStatement(config, entities)
        val (_, keys) = support.insert(config, true) { it.executeUpdate(statement) }
        return keys
    }

    private fun postInsert(entities: List<ENTITY>, generatedKeys: List<Long>): List<ENTITY> {
        return runner.postInsert(entities, generatedKeys)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
