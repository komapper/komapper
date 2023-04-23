package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.single
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.runner.RelationInsertValuesReturningRunner
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDatabaseConfig
import java.sql.ResultSet

internal class JdbcRelationInsertValuesReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
    private val transform: (JdbcDataOperator, ResultSet) -> T,
) : JdbcRunner<T> {

    private val runner: RelationInsertValuesReturningRunner<ENTITY, ID, META> = RelationInsertValuesReturningRunner(context)

    private val support: JdbcRelationInsertValuesSupport<ENTITY, ID, META> = JdbcRelationInsertValuesSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): T {
        return support.handleIdGenerator(
            config,
            object : JdbcRelationInsertValuesSupport.Callback<ENTITY, ID, T> {
                override fun autoIncrement(idGenerator: IdGenerator.AutoIncrement<ENTITY, ID>): T {
                    return insert(config)
                }

                override fun sequence(idGenerator: IdGenerator.Sequence<ENTITY, ID>, id: ID): T {
                    val idAssignment = runner.preInsertUsingSequence(idGenerator, id)
                    return insert(config, idAssignment)
                }

                override fun other(): T {
                    return insert(config)
                }
            },
        )
    }

    private fun insert(
        config: JdbcDatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>? = null,
    ): T {
        val statement = runner.buildStatement(config, idAssignment)
        val executor = config.dialect.createExecutor(config, context.options)
        return executor.executeReturning(statement, transform) { it.single() }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
