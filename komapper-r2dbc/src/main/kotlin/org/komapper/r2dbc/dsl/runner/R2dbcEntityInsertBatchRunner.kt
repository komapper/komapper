package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.hasAutoIncrementProperty
import org.komapper.core.dsl.runner.EntityInsertBatchRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityInsertBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : R2dbcRunner<List<ENTITY>> {

    private val runner: EntityInsertBatchRunner<ENTITY, ID, META> =
        EntityInsertBatchRunner(context, entities)

    private val support: R2dbcEntityInsertRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityInsertRunnerSupport(context)

    override suspend fun run(config: R2dbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        if (!config.dialect.supportsBatchRunOfParameterizedStatement()) {
            throw UnsupportedOperationException("The dialect(driver=${config.dialect.driver}) does not support a batch run.")
        }
        if (context.target.hasAutoIncrementProperty() &&
            !config.dialect.supportsBatchRunReturningGeneratedValues()
        ) {
            throw UnsupportedOperationException("The dialect(driver=${config.dialect.driver}) does not support a batch run for entities with auto-increment properties.")
        }
        val newEntities = preInsert(config)
        val countAndKeyList = insert(config, newEntities)
        return postInsert(newEntities, countAndKeyList)
    }

    private suspend fun preInsert(config: R2dbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private suspend fun insert(config: R2dbcDatabaseConfig, entities: List<ENTITY>): List<Pair<Int, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.insert(config) { it.executeBatch(statements) }
    }

    private fun postInsert(entities: List<ENTITY>, countAndKeyList: List<Pair<Int, Long?>>): List<ENTITY> {
        val iterator = countAndKeyList.map { it.second }.iterator()
        return entities.map { entity ->
            if (iterator.hasNext()) {
                val generatedKey = iterator.next()
                if (generatedKey != null) {
                    support.postInsert(entity, generatedKey)
                } else {
                    entity
                }
            } else {
                entity
            }
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
