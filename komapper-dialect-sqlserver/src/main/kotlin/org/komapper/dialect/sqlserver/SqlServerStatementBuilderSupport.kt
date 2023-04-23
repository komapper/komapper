package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.ReturningProvider
import org.komapper.core.dsl.expression.ColumnExpression

class SqlServerStatementBuilderSupport(
    private val dialect: BuilderDialect,
    private val returningProvider: ReturningProvider,
) {

    fun buildOutput(): Statement {
        val buf = StatementBuffer()
        with(buf) {
            val expressions = returningProvider.returning.expressions()
            if (expressions.isNotEmpty()) {
                append(" output ")
                for (e in expressions) {
                    append("inserted.")
                    column(e)
                    append(", ")
                }
                cutBack(2)
            }
        }
        return buf.toStatement()
    }

    private fun StatementBuffer.column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        append(name)
    }
}
