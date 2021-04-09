package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.JoinKind
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression

internal class SelectStatementBuilderSupport(
    dialect: Dialect,
    private val context: SelectContext<*, *>,
    aliasManager: AliasManager = AliasManagerImpl(context),
    private val buf: StatementBuffer
) {
    private val support = BuilderSupport(dialect, aliasManager, buf)
    private val orderBySupport = OrderByBuilderSupport(dialect, context.orderBy, aliasManager, buf)

    fun selectClause(distinct: Boolean = false) {
        buf.append("select ")
        if (distinct) {
            buf.append("distinct ")
        }
        for (p in context.projection.propertyExpressions()) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
    }

    fun fromClause() {
        buf.append(" from ")
        table(context.entityMetamodel)
        if (context.joins.isNotEmpty()) {
            for (join in context.joins) {
                if (join.kind === JoinKind.INNER) {
                    buf.append(" inner join ")
                } else if (join.kind === JoinKind.LEFT_OUTER) {
                    buf.append(" left outer join ")
                }
                table(join.entityMetamodel)
                if (join.on.isNotEmpty()) {
                    buf.append(" on (")
                    for ((index, criterion) in join.on.withIndex()) {
                        visitCriterion(index, criterion)
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
                visitCriterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
    }

    fun orderByClause() {
        orderBySupport.orderByClause()
    }

    fun offsetLimitClause() {
        if (context.offset >= 0) {
            buf.append(" offset ")
            buf.append(context.offset)
            buf.append(" rows")
        }
        if (context.limit > 0) {
            buf.append(" fetch first ")
            buf.append(context.limit)
            buf.append(" rows only")
        }
    }

    fun forUpdateClause() {
        if (context.forUpdate.option != null) {
            buf.append(" for update")
        }
    }

    private fun table(expression: EntityExpression<*>) {
        support.visitEntityExpression(expression)
    }

    fun column(expression: PropertyExpression<*>) {
        support.visitPropertyExpression(expression)
    }

    fun visitCriterion(index: Int, c: Criterion) {
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
                    is SortItem.Property<*> -> {
                        val (expression, sort) = when (item) {
                            is SortItem.Property.Asc<*> -> item.expression to "asc"
                            is SortItem.Property.Desc<*> -> item.expression to "desc"
                        }
                        column(expression)
                        buf.append(" $sort")
                    }
                    is SortItem.Alias -> {
                        val (alias, sort) = when (item) {
                            is SortItem.Alias.Asc -> item.alias to "asc"
                            is SortItem.Alias.Desc -> item.alias to "desc"
                        }
                        buf.append("${dialect.quote(alias)} $sort")
                    }
                }
                buf.append(", ")
            }
            buf.cutBack(2)
        }
    }

    fun column(expression: PropertyExpression<*>) {
        support.visitPropertyExpression(expression)
    }
}
