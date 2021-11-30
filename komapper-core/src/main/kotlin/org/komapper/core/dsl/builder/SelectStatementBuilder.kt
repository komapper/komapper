package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.InnerJoin
import org.komapper.core.dsl.element.LeftJoin
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.TableExpression

class SelectStatementBuilder(
    val dialect: Dialect,
    val context: SelectContext<*, *, *>,
    aliasManager: AliasManager = DefaultAliasManager(context),
    private val projectionPredicate: (ColumnExpression<*, *>) -> Boolean = { true }
) {
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf, context.options.escapeSequence)
    private val orderBySupport = OrderByBuilderSupport(dialect, context.orderBy, aliasManager, buf)

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
        buf.append("select ")
        if (context.distinct) {
            buf.append("distinct ")
        }
        for (e in context.getProjection().expressions(projectionPredicate)) {
            column(e)
            buf.append(", ")
        }
        buf.cutBack(2)
    }

    private fun fromClause() {
        buf.append(" from ")
        table(context.target)
        if (context.joins.isNotEmpty()) {
            for (join in context.joins) {
                when (join) {
                    is InnerJoin -> buf.append(" inner join ")
                    is LeftJoin -> buf.append(" left outer join ")
                }
                table(join.target)
                val criteria = join.getOnCriteria()
                if (criteria.isNotEmpty()) {
                    buf.append(" on (")
                    for ((index, criterion) in criteria.withIndex()) {
                        criterion(index, criterion)
                        buf.append(" and ")
                    }
                    buf.cutBack(5)
                    buf.append(")")
                }
            }
        }
    }

    private fun whereClause() {
        val criteria = context.getWhereCriteria()
        if (criteria.isNotEmpty()) {
            buf.append(" where ")
            for ((index, criterion) in criteria.withIndex()) {
                criterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
    }

    private fun groupByClause() {
        val groupByItems = context.groupBy.ifEmpty {
            val expressions = context.getProjection().expressions()
            val aggregateFunctions = expressions.filterIsInstance<AggregateFunction<*, *>>()
            val groupByItems = expressions - aggregateFunctions.toSet()
            if (aggregateFunctions.isNotEmpty() && groupByItems.isNotEmpty()) {
                groupByItems
            } else {
                emptyList()
            }
        }
        if (groupByItems.isNotEmpty()) {
            buf.append(" group by ")
            for (item in groupByItems) {
                column(item)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
    }

    private fun havingClause() {
        val criteria = context.getHavingCriteria()
        if (criteria.isNotEmpty()) {
            buf.append(" having ")
            for ((index, criterion) in criteria.withIndex()) {
                criterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
    }

    private fun orderByClause() {
        orderBySupport.orderByClause()
    }

    private fun offsetLimitClause() {
        val builder = dialect.getOffsetLimitStatementBuilder(context.offset, context.limit)
        val statement = builder.build()
        buf.append(statement)
    }

    private fun forUpdateClause() {
        if (context.forUpdate.options != null) {
            buf.append(" for update")
        }
    }

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        support.visitColumnExpression(expression)
    }

    private fun criterion(index: Int, c: Criterion) {
        support.visitCriterion(index, c)
    }
}

internal class OrderByBuilderSupport(
    private val dialect: Dialect,
    private val sortItems: List<SortItem>,
    aliasManager: AliasManager,
    private val buf: StatementBuffer
) {
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun orderByClause() {
        if (sortItems.isNotEmpty()) {
            buf.append(" order by ")
            for (item in sortItems) {
                when (item) {
                    is SortItem.Column -> {
                        val appendColumn = { column(item.expression) }
                        when (item) {
                            is SortItem.Column.Asc -> asc(appendColumn)
                            is SortItem.Column.AscNullsFirst -> ascNullsFirst(appendColumn)
                            is SortItem.Column.AscNullsLast -> ascNullsLast(appendColumn)
                            is SortItem.Column.Desc -> desc(appendColumn)
                            is SortItem.Column.DescNullsFirst -> descNullsFirst(appendColumn)
                            is SortItem.Column.DescNullsLast -> descNullsLast(appendColumn)
                        }
                    }
                    is SortItem.Alias -> {
                        val appendColumn: () -> Unit = { buf.append(dialect.enquote(item.alias)) }
                        when (item) {
                            is SortItem.Alias.Asc -> asc(appendColumn)
                            is SortItem.Alias.AscNullsFirst -> ascNullsFirst(appendColumn)
                            is SortItem.Alias.AscNullsLast -> ascNullsLast(appendColumn)
                            is SortItem.Alias.Desc -> desc(appendColumn)
                            is SortItem.Alias.DescNullsFirst -> descNullsFirst(appendColumn)
                            is SortItem.Alias.DescNullsLast -> descNullsLast(appendColumn)
                        }
                    }
                }
                buf.append(", ")
            }
            buf.cutBack(2)
        }
    }

    private fun column(expression: ColumnExpression<*, *>) {
        support.visitColumnExpression(expression)
    }

    private fun asc(appendColumn: () -> Unit) {
        appendColumn()
        buf.append(" asc")
    }

    private fun desc(appendColumn: () -> Unit) {
        appendColumn()
        buf.append(" desc")
    }

    private fun ascNullsFirst(appendColumn: () -> Unit) {
        if (dialect.supportsNullOrdering()) {
            appendColumn()
            buf.append(" asc nulls first")
        } else {
            buf.append("case when ")
            appendColumn()
            buf.append(" is null then 0 else 1 end asc, ")
            asc(appendColumn)
        }
    }

    private fun ascNullsLast(appendColumn: () -> Unit) {
        if (dialect.supportsNullOrdering()) {
            appendColumn()
            buf.append(" asc nulls last")
        } else {
            buf.append("case when ")
            appendColumn()
            buf.append(" is null then 1 else 0 end asc, ")
            asc(appendColumn)
        }
    }

    private fun descNullsFirst(appendColumn: () -> Unit) {
        if (dialect.supportsNullOrdering()) {
            appendColumn()
            buf.append(" desc nulls first")
        } else {
            buf.append("case when ")
            appendColumn()
            buf.append(" is null then 1 else 0 end desc, ")
            desc(appendColumn)
        }
    }

    private fun descNullsLast(appendColumn: () -> Unit) {
        if (dialect.supportsNullOrdering()) {
            appendColumn()
            buf.append(" desc nulls last")
        } else {
            buf.append("case when ")
            appendColumn()
            buf.append(" is null then 0 else 1 end desc, ")
            desc(appendColumn)
        }
    }
}
