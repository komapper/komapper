package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.toList
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntitySelectStatementBuilder
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntitySelectOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor
import org.komapper.r2dbc.dsl.query.EntityMapper

class EntitySelectQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val option: EntitySelectOption = EntitySelectOption.default
) : R2dbcQueryRunner<List<ENTITY>> {

    // TODO
    override suspend fun run(config: R2dbcDatabaseConfig): List<ENTITY> {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        return executor.executeQuery(statement) { row, _ ->
            val mapper = EntityMapper(config.dialect, row)
            mapper.execute(context.target) as ENTITY
        }.distinctUntilChangedBy { context.target.getId(it) }.toList()
    }

    fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = EntitySelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
