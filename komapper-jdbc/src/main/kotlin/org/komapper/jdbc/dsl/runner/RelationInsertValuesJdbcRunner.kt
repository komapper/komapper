package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.runner.RelationInsertValuesRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class RelationInsertValuesJdbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : JdbcRunner<Pair<Int, ID?>> {

    private val runner: RelationInsertValuesRunner<ENTITY, ID, META> = RelationInsertValuesRunner(context)

    override fun run(config: JdbcDatabaseConfig): Pair<Int, ID?> {
        fun returnWithoutId(): Pair<Int, ID?> {
            val (count, _) = execute(config)
            return count to null
        }

        return when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.AutoIncrement<ENTITY, ID> -> {
                val (count, keys) = execute(config, requiresGeneratedKeys = true)
                val id = keys.firstOrNull()?.let { context.target.convertToId(it) }
                count to id
            }
            is IdGenerator.Sequence<ENTITY, ID> -> {
                if (context.target.disableSequenceAssignment() || context.options.disableSequenceAssignment) {
                    returnWithoutId()
                } else {
                    val id = idGenerator.execute(config, context.options)
                    val argument = Operand.Argument(idGenerator.property, id)
                    val idAssignment = idGenerator.property to argument
                    val (count, _) = execute(config, idAssignment)
                    count to id
                }
            }
            else -> {
                returnWithoutId()
            }
        }
    }

    private fun execute(
        config: JdbcDatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>? = null,
        requiresGeneratedKeys: Boolean = false
    ): Pair<Int, LongArray> {
        val statement = buildStatement(config, idAssignment)
        val executor = JdbcExecutor(config, context.options, requiresGeneratedKeys)
        return executor.executeUpdate(statement)
    }

    private fun buildStatement(
        config: JdbcDatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>?
    ): Statement {
        val clock = config.clockProvider.now()
        return runner.buildStatement(
            config,
            idAssignment,
            context.target.versionAssignment(),
            context.target.createdAtAssignment(clock),
            context.target.updatedAtAssignment(clock)
        )
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
