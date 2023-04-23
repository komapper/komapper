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
) : JdbcRunner<Pair<Long, ID?>> {

    private val runner: RelationInsertValuesRunner<ENTITY, ID, META> = RelationInsertValuesRunner(context)

    private val support: JdbcRelationInsertValuesSupport<ENTITY, ID, META> = JdbcRelationInsertValuesSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): Pair<Long, ID?> {
        return support.handleIdGenerator(
            config,
            object : JdbcRelationInsertValuesSupport.Callback<ENTITY, ID, Pair<Long, ID?>> {
                override fun autoIncrement(idGenerator: IdGenerator.AutoIncrement<ENTITY, ID>): Pair<Long, ID?> {
                    val generatedColumn = runner.preInsertUsingAutoIncrement(idGenerator)
                    val (count, keys) = insert(config, generatedColumn = generatedColumn)
                    return runner.postInsertUsingAutoIncrement(count, keys)
                }

                override fun sequence(idGenerator: IdGenerator.Sequence<ENTITY, ID>, id: ID): Pair<Long, ID?> {
                    val idAssignment = runner.preInsertUsingSequence(idGenerator, id)
                    val (count, _) = insert(config, idAssignment)
                    return count to id
                }

                override fun other(): Pair<Long, ID?> {
                    val (count, _) = insert(config)
                    return count to null
                }
            },
        )
    }

    private fun insert(
        config: JdbcDatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>? = null,
        generatedColumn: String? = null,
    ): Pair<Long, List<Long>> {
        val statement = runner.buildStatement(config, idAssignment)
        val executor = JdbcExecutor(config, context.options, generatedColumn)
        return executor.executeUpdate(statement)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
