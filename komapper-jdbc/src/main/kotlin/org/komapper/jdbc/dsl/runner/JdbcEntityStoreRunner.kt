package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityStore
import org.komapper.core.dsl.runner.EntityStoreFactory
import org.komapper.core.dsl.runner.SelectRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityStoreRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
) : JdbcRunner<EntityStore> {

    private val runner: SelectRunner = SelectRunner(context)
    private val factory: EntityStoreFactory = EntityStoreFactory()

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): EntityStore {
        val metamodels = context.getProjection().metamodels()
        val statement = runner.buildStatement(config)
        val executor = config.dialect.createExecutor(config, context.options)
        val rows = executor.executeQuery(statement) { rs ->
            val rows = mutableListOf<Map<EntityMetamodel<*, *, *>, Any>>()
            while (rs.next()) {
                val row = mutableMapOf<EntityMetamodel<*, *, *>, Any>()
                val mapper = JdbcEntityMapper(config.dataOperator, rs)
                for (metamodel in metamodels) {
                    val entity = mapper.execute(metamodel) ?: continue
                    row[metamodel] = entity
                }
                rows.add(row)
            }
            rows
        }
        return factory.create(metamodels, rows)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
