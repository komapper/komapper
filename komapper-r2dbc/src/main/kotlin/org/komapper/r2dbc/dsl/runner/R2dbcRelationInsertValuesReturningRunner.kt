package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.single
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.runner.RelationInsertValuesReturningRunner
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcRelationInsertValuesReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
    private val transform: (R2dbcDataOperator, Row) -> T,
) : R2dbcRunner<T> {
    private val runner: RelationInsertValuesReturningRunner<ENTITY, ID, META> = RelationInsertValuesReturningRunner(context)

    private val support: R2dbcRelationInsertValuesSupport<ENTITY, ID, META> = R2dbcRelationInsertValuesSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): T {
        return support.handleIdGenerator(
            config,
            object : R2dbcRelationInsertValuesSupport.Callback<ENTITY, ID, T> {
                override suspend fun autoIncrement(idGenerator: IdGenerator.AutoIncrement<ENTITY, ID>): T {
                    return insert(config)
                }

                override suspend fun sequence(idGenerator: IdGenerator.Sequence<ENTITY, ID>, id: ID): T {
                    val idAssignment = runner.preInsertUsingSequence(idGenerator, id)
                    return insert(config, idAssignment)
                }

                override suspend fun other(): T {
                    return insert(config)
                }
            },
        )
    }

    private suspend fun insert(
        config: R2dbcDatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>? = null,
    ): T {
        val statement = runner.buildStatement(config, idAssignment)
        val executor = R2dbcExecutor(config, context.options)
        val flow = executor.executeQuery(statement, transform)
        return flow.single()
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
