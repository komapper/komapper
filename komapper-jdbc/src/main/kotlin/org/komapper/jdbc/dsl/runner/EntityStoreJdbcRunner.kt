package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.query.EntityStore
import org.komapper.core.dsl.runner.EntityStoreFactory
import org.komapper.core.dsl.runner.SelectRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class EntityStoreJdbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
    private val options: SelectOptions,
) : JdbcRunner<EntityStore<ENTITY>> {

    private val runner: SelectRunner = SelectRunner(context, options)
    private val factory: EntityStoreFactory<ENTITY, ID, META> = EntityStoreFactory(context)

    override fun run(config: JdbcDatabaseConfig): EntityStore<ENTITY> {
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        val rows = executor.executeQuery(statement) { rs ->
            val rows = mutableListOf<Map<EntityMetamodel<*, *, *>, Any>>()
            while (rs.next()) {
                val row = mutableMapOf<EntityMetamodel<*, *, *>, Any>()
                val mapper = JdbcEntityMapper(config.dialect, rs)
                for (metamodel in context.getProjection().metamodels()) {
                    val entity = mapper.execute(metamodel) ?: continue
                    row[metamodel] = entity
                }
                rows.add(row)
            }
            rows
        }
        return factory.create(rows)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
