package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

class SqlTripleEntitiesQueryRunner<
    A : Any, A_META : EntityMetamodel<A, *, A_META>,
    B : Any, B_META : EntityMetamodel<B, *, B_META>,
    C : Any, C_META : EntityMetamodel<C, *, C_META>,
    R>(
    private val context: SqlSelectContext<A, *, A_META>,
    private val option: SqlSelectOption,
    private val metamodels: Triple<A_META, B_META, C_META>,
    private val collect: suspend (Flow<Triple<A, B?, C?>>) -> R
) :
    JdbcQueryRunner<R> {

    override fun run(config: DatabaseConfig): R {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val provide: (JdbcDialect, ResultSet) -> Triple<A, B?, C?> = { dialect, rs ->
            val mapper = EntityMapper(dialect, rs)
            val first = mapper.execute(metamodels.first, true)
            val second = mapper.execute(metamodels.second)
            val third = mapper.execute(metamodels.third)
            Triple(checkNotNull(first), second, third)
        }
        val executor = JdbcExecutor(config, option)
        return executor.executeQuery(statement, provide, collect)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).asSql()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
