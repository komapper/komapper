package org.komapper.dialect.h2

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.ReturningProvider
import org.komapper.core.dsl.expression.ColumnExpression

class H2StatementBuilderSupport(
    private val dialect: BuilderDialect,
    private val returningProvider: ReturningProvider,
) {
    fun buildReturningFirstFragment(tableType: DeltaTableType = DeltaTableType.FINAL): Statement {
        val expressions = returningProvider.returning.expressions()
        return with(StatementBuffer()) {
            if (expressions.isNotEmpty()) {
                append("select ")
                for (e in expressions) {
                    column(e)
                    append(", ")
                }
                cutBack(2)
                val tableTypeName = when (tableType) {
                    DeltaTableType.OLD -> "old"
                    DeltaTableType.FINAL -> "final"
                }
                append(" from $tableTypeName table (")
            }
            toStatement()
        }
    }

    fun buildReturningLastFragment(): Statement {
        val expressions = returningProvider.returning.expressions()
        return with(StatementBuffer()) {
            if (expressions.isNotEmpty()) {
                append(")")
            }
            toStatement()
        }
    }

    private fun StatementBuffer.column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        append(name)
    }

    enum class DeltaTableType {
        OLD,
        FINAL,
    }
}
