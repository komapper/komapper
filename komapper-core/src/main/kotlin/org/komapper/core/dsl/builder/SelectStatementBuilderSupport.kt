package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.JoinKind
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo

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
            is Projection.Columns -> projection.values
            is Projection.Tables -> projection.values.flatMap { it.properties() }
        }
        for (c in columns) {
            visitColumnInfo(c)
            buf.append(", ")
        }
        buf.cutBack(2)
    }

    fun fromClause() {
        buf.append(" from ")
        visitTableInfo(context.entityMetamodel)
        if (context.joins.isNotEmpty()) {
            for (join in context.joins) {
                if (join.kind === JoinKind.INNER) {
                    buf.append(" inner join ")
                } else if (join.kind === JoinKind.LEFT_OUTER) {
                    buf.append(" left outer join ")
                }
                visitTableInfo(join.entityMetamodel)
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
                val (columnInfo, sort) = when (item) {
                    is SortItem.Asc<*> -> item.columnInfo to "asc"
                    is SortItem.Desc<*> -> item.columnInfo to "desc"
                    else -> item to null
                }
                visitColumnInfo(columnInfo)
                if (sort != null) {
                    buf.append(" $sort")
                }
                buf.append(", ")
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

    private fun visitTableInfo(tableInfo: TableInfo) {
        support.visitTableInfo(tableInfo)
    }

    fun visitColumnInfo(columnInfo: ColumnInfo<*>) {
        support.visitColumnInfo(columnInfo)
    }

    fun visitCriterion(index: Int, c: Criterion) {
        return support.visitCriterion(index, c)
    }
}
