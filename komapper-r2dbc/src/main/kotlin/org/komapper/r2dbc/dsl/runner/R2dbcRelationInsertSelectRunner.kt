package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.runner.RelationInsertSelectRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcRelationInsertSelectRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertSelectContext<ENTITY, ID, META>,
) : R2dbcRunner<Pair<Int, List<ID>>> {

    private val runner: RelationInsertSelectRunner<ENTITY, ID, META> = RelationInsertSelectRunner(context)

    override suspend fun run(config: R2dbcDatabaseConfig): Pair<Int, List<ID>> {
        val statement = runner.buildStatement(config)
        val generatedColumn = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.AutoIncrement<ENTITY, *> -> idGenerator.property.columnName
            else -> null
        }
        val executor = R2dbcExecutor(config, context.options, generatedColumn)
        val (count, keys) = executor.executeUpdate(statement)
        val ids = keys.map { context.target.convertToId(it) }.filterNotNull()
        return count to ids
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
