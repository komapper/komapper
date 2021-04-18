package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class SqlUpdateStatementBuilder<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    val dialect: Dialect,
    val context: SqlUpdateContext<ENTITY, META>,
) {

    private val aliasManager = AliasManagerImpl(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        buf.append("update ")
        table(context.entityMetamodel)
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

    private fun table(expression: EntityExpression<*>) {
        support.visitEntityExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    private fun column(expression: PropertyExpression<*>) {
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
