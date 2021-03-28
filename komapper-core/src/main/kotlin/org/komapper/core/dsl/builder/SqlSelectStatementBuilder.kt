package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.data.Criterion
import org.komapper.core.dsl.expr.AggregateFunction
import org.komapper.core.metamodel.ColumnInfo

internal class SqlSelectStatementBuilder<ENTITY>(
    val dialect: Dialect,
    val context: SqlSelectContext<ENTITY>,
    aliasManager: AliasManager = AliasManager(context)
) {
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = SelectStatementBuilderSupport(dialect, context, aliasManager, buf)

    fun build(): Statement {
        selectClause()
        fromClause()
        whereClause()
        groupByClause()
        havingClause()
        orderByClause()
        offsetLimitClause()
        forUpdateClause()
        return buf.toStatement()
    }

    private fun selectClause() {
        support.selectClause()
    }

    private fun fromClause() {
        support.fromClause()
    }

    private fun whereClause() {
        support.whereClause()
    }

    private fun groupByClause() {
        if (context.groupBy.isEmpty()) {
            val columns = context.getProjectionColumns()
            val aggregateFunctions = columns.filter { it is AggregateFunction }
            val groupByItems = columns - aggregateFunctions
            if (aggregateFunctions.isNotEmpty() && groupByItems.isNotEmpty()) {
                buf.append(" group by ")
                for (item in groupByItems) {
                    visitColumnInfo(item)
                    buf.append(", ")
                }
                buf.cutBack(2)
            }
        } else {
            buf.append(" group by ")
            for (item in context.groupBy) {
                visitColumnInfo(item)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
    }

    private fun havingClause() {
        if (context.having.isNotEmpty()) {
            buf.append(" having ")
            for ((index, criterion) in context.having.withIndex()) {
                visitCriterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
    }

    private fun orderByClause() {
        support.orderByClause()
    }

    private fun offsetLimitClause() {
        support.offsetLimitClause()
    }

    private fun forUpdateClause() {
        support.forUpdateClause()
    }

    private fun visitColumnInfo(columnInfo: ColumnInfo<*>) {
        support.visitColumnInfo(columnInfo)
    }

    private fun visitCriterion(index: Int, c: Criterion) {
        return support.visitCriterion(index, c)
    }
}
