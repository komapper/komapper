package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

class PairEntitiesProjectedQueryRunner<A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, R>(
    private val context: SqlSelectContext<A, *, A_META>,
    private val option: SqlSelectOption,
    private val provide: (JdbcDialect, ResultSet) -> Pair<A, B?>,
    private val transform: (Sequence<Pair<A, B?>>) -> R
) :
    JdbcQueryRunner<R> {

    override fun run(config: DatabaseConfig): R {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
        return executor.executeQuery(statement, provide, transform)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
