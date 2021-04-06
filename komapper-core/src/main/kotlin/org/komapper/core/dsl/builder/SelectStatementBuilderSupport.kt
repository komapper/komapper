package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.JoinKind
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.expr.EntityExpression
import org.komapper.core.dsl.expr.NamedSortItem
import org.komapper.core.dsl.expr.PropertyExpression

internal class SelectStatementBuilderSupport(
    dialect: Dialect,
    private val context: SelectContext<*, *>,
    aliasManager: AliasManager = AliasManager(context),
    private val buf: StatementBuffer
) {
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun selectClause() {
        buf.append("select ")
        val columns = when (val projection = context.projection) {
            is Projection.Properties -> projection.values
            is Projection.Entities -> projection.values.flatMap { it.properties() }
        }
        for (c in columns) {
            column(c)
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
        if (context.orderBy.isNotEmpty()) {
            buf.append(" order by ")
            for (item in context.orderBy) {
                val (expression, sort) = when (item) {
                    is NamedSortItem.Asc<*> -> item.expression to "asc"
                    is NamedSortItem.Desc<*> -> item.expression to "desc"
                }
                column(expression)
                buf.append(" $sort, ")
            }
            buf.cutBack(2)
        }
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

    private fun table(expression: EntityExpression) {
        support.visitEntityExpression(expression)
    }

    fun column(expression: PropertyExpression<*>) {
        support.visitPropertyExpression(expression)
    }

    fun visitCriterion(index: Int, c: Criterion) {
        support.visitCriterion(index, c)
    }
}
