package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.ColumnsImpl
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

class SqlMultipleColumnsQueryRunner<R>(
    private val context: SqlSelectContext<*, *, *>,
    private val option: SqlSelectOption,
    private val expressions: List<ColumnExpression<*, *>>,
    private val collect: suspend (Flow<Columns>) -> R
) : JdbcQueryRunner<R> {

    override fun run(config: DatabaseConfig): R {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val provide: (JdbcDialect, ResultSet) -> Columns = { dialect, rs ->
            val mapper = PropertyMapper(dialect, rs)
            val map = expressions.associateWith { mapper.execute(it) }
            ColumnsImpl(map)
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
