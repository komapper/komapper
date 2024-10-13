package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty
import org.komapper.core.dsl.runner.RelationInsertSelectRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcRelationInsertSelectRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertSelectContext<ENTITY, ID, META>,
) : JdbcRunner<Pair<Long, List<ID>>> {
    private val runner: RelationInsertSelectRunner<ENTITY, ID, META> = RelationInsertSelectRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): Pair<Long, List<ID>> {
        val statement = runner.buildStatement(config)
        val generatedColumn = if (context.options.returnGeneratedKeys) {
            context.target.getAutoIncrementProperty()?.columnName
        } else {
            null
        }
        val executor = config.dialect.createExecutor(config, context.options, generatedColumn)
        val (count, keys) = executor.executeUpdate(statement)
        val ids = keys.mapNotNull { context.target.convertToId(it) }
        return count to ids
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
