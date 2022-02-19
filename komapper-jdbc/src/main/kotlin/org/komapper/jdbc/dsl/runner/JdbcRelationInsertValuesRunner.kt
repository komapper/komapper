package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.runner.RelationInsertValuesRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcRelationInsertValuesRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : JdbcRunner<Pair<Int, ID?>> {

    private val runner: RelationInsertValuesRunner<ENTITY, ID, META> = RelationInsertValuesRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): Pair<Int, ID?> {
        fun returnWithoutId(): Pair<Int, ID?> {
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

    private fun insert(
        config: JdbcDatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>? = null,
        generatedColumn: String? = null
    ): Pair<Int, List<Long>> {
        val statement = runner.buildStatement(config, idAssignment)
        val executor = JdbcExecutor(config, context.options, generatedColumn)
        return executor.executeUpdate(statement)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
