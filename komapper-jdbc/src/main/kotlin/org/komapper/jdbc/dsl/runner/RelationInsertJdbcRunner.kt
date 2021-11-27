package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationInsertContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.runner.RelationInsertRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class RelationInsertJdbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertContext<ENTITY, ID, META>,
) : JdbcRunner<Pair<Int, ID?>> {

    private val runner: RelationInsertRunner<ENTITY, ID, META> = RelationInsertRunner(context)

    override fun run(config: JdbcDatabaseConfig): Pair<Int, ID?> {
        val pair = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.Sequence<ENTITY, ID> ->
                if (!context.target.disableSequenceAssignment() && !context.options.disableSequenceAssignment) {
                    val id = idGenerator.execute(config, context.options)
                    val argument = Operand.Argument(idGenerator.property, id)
                    val idAssignment = idGenerator.property to argument
                    id to idAssignment
                } else null
            else -> null
        }
        val clock = config.clockProvider.now()
        val statement =
            runner.buildStatement(
                config,
                pair?.second,
                context.target.versionAssignment(),
                context.target.createdAtAssignment(clock),
                context.target.updatedAtAssignment(clock)
            )
        val requiresGeneratedKeys = context.target.idGenerator() is IdGenerator.AutoIncrement<ENTITY, *>
        val executor = JdbcExecutor(config, context.options, requiresGeneratedKeys)
        val (count, keys) = executor.executeUpdate(statement)
        val id = pair?.first ?: (keys.firstOrNull()?.let { context.target.toId(it) })
        return count to id
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
