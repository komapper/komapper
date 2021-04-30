package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class SqlUpdateStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: SqlUpdateContext<ENTITY, ID, META>,
) {

    private val aliasManager = DefaultAliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        buf.append("update ")
        table(context.target)
        buf.append(" set ")
        for ((left, right) in context.set) {
            column(left)
            buf.append(" = ")
            operand(right)
            buf.append(", ")
        }
        buf.cutBack(2)
        if (context.where.isNotEmpty()) {
            buf.append(" where ")
            for ((index, criterion) in context.where.withIndex()) {
                criterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        return buf.toStatement()
    }

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }

    private fun criterion(index: Int, c: Criterion) {
        return support.visitCriterion(index, c)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }
}
