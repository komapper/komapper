package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.query.EntityContext
import org.komapper.core.dsl.runner.EntityContextFactory
import org.komapper.core.dsl.runner.SelectRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class EntityContextJdbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
    private val options: SelectOptions,
) : JdbcRunner<EntityContext<ENTITY>> {

    private val runner: SelectRunner = SelectRunner(context, options)
    private val factory: EntityContextFactory<ENTITY, ID, META> = EntityContextFactory(context)

    override fun run(config: JdbcDatabaseConfig): EntityContext<ENTITY> {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        val rows = executor.executeQuery(statement) { rs ->
            val rows = mutableListOf<Map<EntityMetamodel<*, *, *>, Any>>()
            while (rs.next()) {
                val row = mutableMapOf<EntityMetamodel<*, *, *>, Any>()
                val mapper = JdbcEntityMapper(config.dialect, rs)
                for (metamodel in context.projection.metamodels()) {
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
