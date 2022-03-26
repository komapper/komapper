package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class RelationUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: RelationUpdateContext<ENTITY, ID, META>,
) {

    private val aliasManager = if (dialect.supportsAliasForUpdateStatement()) {
        DefaultAliasManager(context)
    } else {
        EmptyAliasManager
    }

    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf, context.options.escapeSequence)

    fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        buf.append("update ")
        table(context.target)
        buf.append(" set ")
        if (assignments.isNotEmpty()) {
            for ((left, right) in assignments) {
                column(left)
                buf.append(" = ")
                operand(right)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        val version = context.target.versionProperty()
        if (version != null && version !in assignments.map { it.first }) {
            if (assignments.isNotEmpty()) {
                buf.append(", ")
            }
            column(version)
            buf.append(" = ")
            column(version)
            buf.append(" + 1")
        }
        val criteria = context.getWhereCriteria()
        if (criteria.isNotEmpty()) {
            buf.append(" where ")
            for ((index, criterion) in criteria.withIndex()) {
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
