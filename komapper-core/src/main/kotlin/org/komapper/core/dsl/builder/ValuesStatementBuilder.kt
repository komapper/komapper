package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.ValuesContext

class ValuesStatementBuilder(
    private val dialect: BuilderDialect,
    private val context: ValuesContext<*, *, *>,
    private val aliasManager: AliasManager,
    private val useSelectUnionForm: Boolean = !dialect.supportsAliasColumnListInDerivedTable(),
) {
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        if (!dialect.supportsTableValueConstructor()) {
            throw UnsupportedOperationException(
                "The dialect(driver=${dialect.driver}) does not support the VALUES table value constructor."
            )
        }
        require(context.rows.isNotEmpty()) {
            "The VALUES clause requires at least one row."
        }
        if (useSelectUnionForm) {
            buildSelectUnionForm()
        } else {
            buildValuesForm()
        }
        return buf.toStatement()
    }

    private fun buildValuesForm() {
        val properties = context.target.properties()
        val rowOpen = if (dialect.supportsRowKeywordInTableValueConstructor()) "row(" else "("
        buf.append("values ")
        for (row in context.rows) {
            val byProperty = row.toMap()
            buf.append(rowOpen)
            for (p in properties) {
                val operand = byProperty[p]
                    ?: error("Row is missing assignment for property '${p.name}'.")
                support.visitOperand(operand)
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append("), ")
        }
        buf.cutBack(2)
    }

    private fun buildSelectUnionForm() {
        val properties = context.target.properties()
        for ((rowIndex, row) in context.rows.withIndex()) {
            if (rowIndex > 0) {
                buf.append(" union all ")
            }
            buf.append("select ")
            val byProperty = row.toMap()
            for (p in properties) {
                val operand = byProperty[p]
                    ?: error("Row is missing assignment for property '${p.name}'.")
                support.visitOperand(operand)
                if (rowIndex == 0) {
                    buf.append(" as ")
                    buf.append(p.getCanonicalColumnName(dialect::enquote))
                }
                buf.append(", ")
            }
            buf.cutBack(2)
        }
    }
}
