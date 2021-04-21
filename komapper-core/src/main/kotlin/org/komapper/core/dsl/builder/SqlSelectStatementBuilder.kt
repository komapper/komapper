package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.PropertyExpression

internal class SqlSelectStatementBuilder(
    val dialect: Dialect,
    val context: SqlSelectContext<*, *, *>,
    aliasManager: AliasManager = AliasManagerImpl(context)
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
        support.selectClause(context.distinct)
    }

    private fun fromClause() {
        support.fromClause()
    }

    private fun whereClause() {
        support.whereClause()
    }

    private fun groupByClause() {
        if (context.groupBy.isEmpty()) {
            val propertyExpressions = context.projection.propertyExpressions()
            val aggregateFunctions = propertyExpressions.filterIsInstance<AggregateFunction<*>>()
            val groupByItems = propertyExpressions - aggregateFunctions
            if (aggregateFunctions.isNotEmpty() && groupByItems.isNotEmpty()) {
                buf.append(" group by ")
                for (item in groupByItems) {
                    column(item)
                    buf.append(", ")
                }
                buf.cutBack(2)
            }
        } else {
            buf.append(" group by ")
            for (item in context.groupBy) {
                column(item)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
    }

    private fun havingClause() {
        if (context.having.isNotEmpty()) {
            buf.append(" having ")
            for ((index, criterion) in context.having.withIndex()) {
                criterion(index, criterion)
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

    private fun column(expression: PropertyExpression<*>) {
        support.column(expression)
    }

    private fun criterion(index: Int, c: Criterion) {
        support.criterion(index, c)
    }
}
