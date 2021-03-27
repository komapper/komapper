package org.komapper.core.query.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.StatementBuffer
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo
import org.komapper.core.query.context.JoinKind
import org.komapper.core.query.context.SelectContext
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.SortItem

internal class SelectStatementBuilderSupport<ENTITY>(
    dialect: Dialect,
    private val context: SelectContext<ENTITY, *>,
    aliasManager: AliasManager = AliasManager(context),
    private val buf: StatementBuffer
) {
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun selectClause() {
        buf.append("select ")
        val columns = context.getProjectionColumns()
        columns.joinTo(buf) { columnName(it) }
    }

    fun fromClause() {
        buf.append(" from ")
        buf.append(tableName(context.entityMetamodel))
        if (context.joins.isNotEmpty()) {
            for (join in context.joins) {
                if (join.kind === JoinKind.INNER) {
                    buf.append(" inner join ")
                } else if (join.kind === JoinKind.LEFT_OUTER) {
                    buf.append(" left outer join ")
                }
                buf.append(tableName(join.entityMetamodel))
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
                buf.append(columnName(columnInfo))
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

    fun tableName(tableInfo: TableInfo): String {
        return support.tableName(tableInfo)
    }

    fun columnName(columnInfo: ColumnInfo<*>): String {
        return support.columnName(columnInfo)
    }

    fun visitCriterion(index: Int, c: Criterion) {
        return support.visitCriterion(index, c)
    }
}
