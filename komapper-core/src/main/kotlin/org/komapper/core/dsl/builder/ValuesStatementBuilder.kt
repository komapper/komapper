package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.ValuesContext

class ValuesStatementBuilder(
    private val dialect: BuilderDialect,
    private val context: ValuesContext,
) {
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, EmptyAliasManager, buf)

    fun build(): Statement {
        buf.append("values ")
        for (row in context.rows) {
            buf.append("(")
            for (expression in row) {
                support.visitColumnExpression(expression)
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append("), ")
        }
        buf.cutBack(2)
        return buf.toStatement()
    }
}
