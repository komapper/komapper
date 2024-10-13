package org.komapper.dialect.oracle

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.ReturningProvider
import org.komapper.core.dsl.expression.ColumnExpression

class OracleStatementBuilderSupport(
    private val dialect: BuilderDialect,
    private val returningProvider: ReturningProvider,
) {
    fun buildReturning(): Statement {
        return with(StatementBuffer()) {
            val expressions = returningProvider.returning.expressions()
            if (expressions.isNotEmpty()) {
                append("returning ")
                for (e in expressions) {
                    column(e)
                    append(", ")
                }
                cutBack(2)
                append(" into ")
                for (e in expressions) {
                    registerReturnParameter(e.interiorType)
                    append(", ")
                }
                cutBack(2)
            }
            toStatement()
        }
    }

    private fun StatementBuffer.column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        append(name)
    }
}
