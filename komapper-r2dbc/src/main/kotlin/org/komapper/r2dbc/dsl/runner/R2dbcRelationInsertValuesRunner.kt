package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.runner.RelationInsertValuesRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcRelationInsertValuesRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : R2dbcRunner<Pair<Long, ID?>> {

    private val runner: RelationInsertValuesRunner<ENTITY, ID, META> = RelationInsertValuesRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): Pair<Long, ID?> {
        suspend fun returnWithoutId(): Pair<Long, ID?> {
            val (count, _) = insert(config)
            return count to null
        }

        return when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.AutoIncrement<ENTITY, ID> -> {
                val generatedColumn = runner.preInsertUsingAutoIncrement(idGenerator)
                val (count, keys) = insert(config, generatedColumn = generatedColumn)
                runner.postInsertUsingAutoIncrement(count, keys)
            }
            is IdGenerator.Sequence<ENTITY, ID> -> {
                if (context.target.disableSequenceAssignment() || context.options.disableSequenceAssignment) {
                    returnWithoutId()
                } else {
                    val id = idGenerator.execute(config, context.options)
                    val idAssignment = runner.preInsertUsingSequence(idGenerator, id)
                    val (count, _) = insert(config, idAssignment)
                    count to id
                }
            }
            else -> {
                returnWithoutId()
            }
        }
    }

    private suspend fun insert(
        config: R2dbcDatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>? = null,
        generatedColumn: String? = null,
    ): Pair<Long, List<Long>> {
        val statement = runner.buildStatement(config, idAssignment)
        val executor = R2dbcExecutor(config, context.options, generatedColumn)
        return executor.executeUpdate(statement)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
