package org.komapper.core.query.builder

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.JoinKind
import org.komapper.core.query.context.SelectContext
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.Operand
import org.komapper.core.query.data.SortItem

internal class SelectStatementBuilder<ENTITY>(val config: DefaultDatabaseConfig, val context: SelectContext<ENTITY>) {
    private val aliasManager = AliasManager(context)
    private val buf = StatementBuffer(config.dialect::formatValue)

    fun build(): Statement {
        buf.append("select ")
        val properties = context.getProjectionTargets().flatMap { it.properties() }
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
        val alias = aliasManager.getAlias(entityMetamodel) ?: error("no alias")
        return entityMetamodel.tableName() + " " + alias
    }

    private fun columnName(propertyMetamodel: PropertyMetamodel<*, *>): String {
        val alias = aliasManager.getAlias(propertyMetamodel) ?: error("no alias")
        return alias + "." + propertyMetamodel.columnName
    }

    private fun visitCriterion(index: Int, criterion: Criterion) {
        when (criterion) {
            is Criterion.Eq -> binaryOperation(criterion.left, criterion.right, "=")
            is Criterion.NotEq -> binaryOperation(criterion.left, criterion.right, "<>")
            is Criterion.Less -> binaryOperation(criterion.left, criterion.right, "<")
            is Criterion.LessEq -> binaryOperation(criterion.left, criterion.right, "<=")
            is Criterion.Grater -> binaryOperation(criterion.left, criterion.right, ">")
            is Criterion.GraterEq -> binaryOperation(criterion.left, criterion.right, ">=")
            is Criterion.InList -> inListOperation(criterion.left, criterion.right)
            is Criterion.NotInList -> inListOperation(criterion.left, criterion.right, true)
            is Criterion.And -> logicalBinaryOperation("and", criterion.criteria, index)
            is Criterion.Or -> logicalBinaryOperation("or", criterion.criteria, index)
            is Criterion.Not -> notOperation(criterion.criteria)
        }
    }

    private fun binaryOperation(left: Operand, right: Operand, operator: String) {
        visitOperand(left)
        buf.append(" $operator ")
        visitOperand(right)
    }

    private fun inListOperation(left: Operand, right: List<Operand>, not: Boolean = false) {
        visitOperand(left)
        if (not) {
            buf.append(" not")
        }
        buf.append(" in (")
        if (right.isEmpty()) {
            buf.append("null")
        } else {
            for (parameter in right) {
                visitOperand(parameter)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        buf.append(")")
    }

    private fun logicalBinaryOperation(operator: String, criteria: List<Criterion>, index: Int) {
        if (criteria.isNotEmpty()) {
            if (index > 0) {
                buf.cutBack(5)
            }
            if (index != 0) {
                buf.append(" $operator ")
            }
            buf.append("(")
            for ((i, c) in criteria.withIndex()) {
                visitCriterion(i, c)
                buf.append(" and ")
            }
            buf.cutBack(5)
            buf.append(")")
        }
    }

    private fun notOperation(criteria: List<Criterion>) {
        if (criteria.isNotEmpty()) {
            buf.append("not ")
            buf.append("(")
            for ((index, c) in criteria.withIndex()) {
                visitCriterion(index, c)
                buf.append(" and ")
            }
            buf.cutBack(5)
            buf.append(")")
        }
    }

    private fun visitOperand(operand: Operand) {
        when (operand) {
            is Operand.Property -> {
                buf.append(columnName(operand.metamodel))
            }
            is Operand.Parameter -> {
                buf.bind(Value(operand.value, operand.metamodel.klass))
            }
        }
    }
}
