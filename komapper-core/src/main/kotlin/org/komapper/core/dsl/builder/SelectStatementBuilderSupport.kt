package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.JoinKind
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.TableExpression

internal class SelectStatementBuilderSupport(
    private val dialect: Dialect,
    private val context: SelectContext<*, *, *, *>,
    aliasManager: AliasManager = DefaultAliasManager(context),
    private val buf: StatementBuffer
) {
    private val support = BuilderSupport(dialect, aliasManager, buf)
    private val orderBySupport = OrderByBuilderSupport(dialect, context.orderBy, aliasManager, buf)

    fun selectClause(distinct: Boolean = false) {
        buf.append("select ")
        if (distinct) {
            buf.append("distinct ")
        }
        for (e in context.projection.expressions()) {
            column(e)
            buf.append(", ")
        }
        buf.cutBack(2)
    }

    fun fromClause() {
        buf.append(" from ")
        table(context.target)
        if (context.joins.isNotEmpty()) {
            for (join in context.joins) {
                if (join.kind === JoinKind.INNER) {
                    buf.append(" inner join ")
                } else if (join.kind === JoinKind.LEFT_OUTER) {
                    buf.append(" left outer join ")
                }
                table(join.target)
                if (join.on.isNotEmpty()) {
                    buf.append(" on (")
                    for ((index, criterion) in join.on.withIndex()) {
                        criterion(index, criterion)
                        buf.append(" and ")
                    }
                    buf.cutBack(5)
                    buf.append(")")
                }
            }
        }
    }

    fun whereClause() {
        if (context.where.isNotEmpty()) {
            buf.append(" where ")
            for ((index, criterion) in context.where.withIndex()) {
                criterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
    }

    fun orderByClause() {
        orderBySupport.orderByClause()
    }

    fun offsetLimitClause() {
        val builder = dialect.getOffsetLimitStatementBuilder(context.offset, context.limit)
        val statement = builder.build()
        buf.append(statement)
    }

    fun forUpdateClause() {
        if (context.forUpdate.options != null) {
            buf.append(" for update")
        }
    }

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    fun column(expression: ColumnExpression<*, *>) {
        support.visitColumnExpression(expression)
    }

    fun criterion(index: Int, c: Criterion) {
        support.visitCriterion(index, c)
    }
}

internal class OrderByBuilderSupport(
    private val dialect: Dialect,
    private val orderBy: List<SortItem>,
    aliasManager: AliasManager,
    private val buf: StatementBuffer
) {
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun orderByClause() {
        if (orderBy.isNotEmpty()) {
            buf.append(" order by ")
            for (item in orderBy) {
                when (item) {
                    is SortItem.Property<*, *> -> {
                        val (expression, sort) = when (item) {
                            is SortItem.Property.Asc<*, *> -> item.expression to "asc"
                            is SortItem.Property.Desc<*, *> -> item.expression to "desc"
                        }
                        column(expression)
                        buf.append(" $sort")
                    }
                    is SortItem.Alias -> {
                        val (alias, sort) = when (item) {
                            is SortItem.Alias.Asc -> item.alias to "asc"
                            is SortItem.Alias.Desc -> item.alias to "desc"
                        }
                        buf.append("${dialect.enquote(alias)} $sort")
                    }
                }
                buf.append(", ")
            }
            buf.cutBack(2)
        }
    }

    fun column(expression: ColumnExpression<*, *>) {
        support.visitColumnExpression(expression)
    }
}
