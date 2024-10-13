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
    fun buildOutput(tablePrefix: TablePrefix = TablePrefix.INSERTED): Statement {
        val buf = StatementBuffer()
        with(buf) {
            val expressions = returningProvider.returning.expressions()
            if (expressions.isNotEmpty()) {
                append("output ")
                val prefix = when (tablePrefix) {
                    TablePrefix.DELETED -> "deleted"
                    TablePrefix.INSERTED -> "inserted"
                }
                for (e in expressions) {
                    append(prefix)
                    append(".")
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

    enum class TablePrefix {
        DELETED,
        INSERTED
    }
}
