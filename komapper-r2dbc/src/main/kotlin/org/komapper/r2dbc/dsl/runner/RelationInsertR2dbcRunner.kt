package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.runner.RelationInsertRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class RelationInsertR2dbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertContext<ENTITY, ID, META>,
    private val options: InsertOptions
) : R2dbcRunner<Pair<Int, ID?>> {

    private val runner: RelationInsertRunner<ENTITY, ID, META> = RelationInsertRunner(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): Pair<Int, ID?> {
        val statement = runner.buildStatement(config)
        val generatedColumn = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.AutoIncrement<ENTITY, *> -> idGenerator.property.columnName
            else -> null
        }
        val executor = R2dbcExecutor(config, options, generatedColumn)
        val (count, keys) = executor.executeUpdate(statement)
        val id = keys.firstOrNull()?.let { context.target.toId(it) }
        return count to id
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
