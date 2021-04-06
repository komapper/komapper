package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expr.EntityExpression

internal class SqlUpdateStatementBuilder<ENTITY>(
    val dialect: Dialect,
    val context: SqlUpdateContext<ENTITY>,
) {
    private val aliasManager = AliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        buf.append("update ")
        table(context.entityMetamodel)
        buf.append(" set ")
        for ((left, right) in context.set) {
            visitOperand(left)
            buf.append(" = ")
            visitOperand(right)
            buf.append(", ")
        }
        buf.cutBack(2)
        if (context.where.isNotEmpty()) {
            buf.append(" where ")
            for ((index, criterion) in context.where.withIndex()) {
                visitCriterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        return buf.toStatement()
    }

    private fun table(expression: EntityExpression) {
        support.visitEntityExpression(expression)
    }

    private fun visitCriterion(index: Int, c: Criterion) {
        return support.visitCriterion(index, c)
    }

    private fun visitOperand(operand: Operand) {
        support.visitOperand(operand)
    }
}
