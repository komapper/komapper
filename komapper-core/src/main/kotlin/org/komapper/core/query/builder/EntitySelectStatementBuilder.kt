package org.komapper.core.query.builder

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.EntitySelectContext
import org.komapper.core.query.context.JoinKind
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.SortItem

internal class EntitySelectStatementBuilder<ENTITY>(
    val config: DatabaseConfig,
    val context: EntitySelectContext<ENTITY>,
    aliasManager: AliasManager = AliasManager(context)
) {
    private val buf = StatementBuffer(config.dialect::formatValue)
    private val support = BuilderSupport(config, aliasManager, buf)

    fun build(): Statement {
        buf.append("select ")
        val properties = context.getProjectionPropertyMetamodels()
        properties.joinTo(buf) { columnName(it) }
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
                if (join.isNotEmpty()) {
                    buf.append(" on (")
                    for ((index, criterion) in join.withIndex()) {
                        visitCriterion(index, criterion)
                        buf.append(" and ")
                    }
                    buf.cutBack(5)
                    buf.append(")")
                }
            }
        }
        if (context.where.isNotEmpty()) {
            buf.append(" where ")
            for ((index, criterion) in context.where.withIndex()) {
                visitCriterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        if (context.orderBy.isNotEmpty()) {
            buf.append(" order by ")
            for (item in context.orderBy) {
                buf.append(columnName(item.propertyMetamodel))
                val sort = when (item) {
                    is SortItem.Asc<*, *> -> "asc"
                    is SortItem.Desc<*, *> -> "desc"
                }
                buf.append(" $sort, ")
            }
            buf.cutBack(2)
        }

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

        if (context.forUpdate.option != null) {
            buf.append(" for update")
        }

        return buf.toStatement()
    }

    private fun tableName(entityMetamodel: EntityMetamodel<*>): String {
        return support.tableName(entityMetamodel)
    }

    private fun columnName(propertyMetamodel: PropertyMetamodel<*, *>): String {
        return support.columnName(propertyMetamodel)
    }

    private fun visitCriterion(index: Int, c: Criterion) {
        return support.visitCriterion(index, c)
    }
}
