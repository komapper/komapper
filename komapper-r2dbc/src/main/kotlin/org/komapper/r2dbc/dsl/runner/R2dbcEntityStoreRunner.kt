package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityStore
import org.komapper.core.dsl.query.ProjectionType
import org.komapper.core.dsl.runner.EntityStoreFactory
import org.komapper.core.dsl.runner.SelectRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcEntityStoreRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
) : R2dbcRunner<EntityStore> {
    private val runner: SelectRunner = SelectRunner(context)
    private val factory: EntityStoreFactory = EntityStoreFactory()

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): EntityStore {
        val metamodels = context.getProjection().metamodels()
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, context.options)
        val rows: Flow<Map<EntityMetamodel<*, *, *>, Any>> = executor.executeQuery(statement) { dataOperator, r2dbcRow ->
            val row = mutableMapOf<EntityMetamodel<*, *, *>, Any>()
            val mapper = R2dbcEntityMapper(ProjectionType.INDEX, dataOperator, r2dbcRow)
            for (metamodel in metamodels) {
                val entity = mapper.execute(metamodel) ?: continue
                row[metamodel] = entity
            }
            row
        }
        return factory.create(metamodels, rows.toList())
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
