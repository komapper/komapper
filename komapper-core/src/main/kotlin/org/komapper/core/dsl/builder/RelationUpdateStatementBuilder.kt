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

interface RelationUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> RelationUpdateStatementBuilder(
    dialect: BuilderDialect,
    context: RelationUpdateContext<ENTITY, ID, META>,
): RelationUpdateStatementBuilder<ENTITY, ID, META> {
    return DefaultRelationUpdateStatementBuilder(dialect, context)
}

class DefaultRelationUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: RelationUpdateContext<ENTITY, ID, META>,
) : RelationUpdateStatementBuilder<ENTITY, ID, META> {
    private val aliasManager = if (dialect.supportsAliasForUpdateStatement()) {
        DefaultAliasManager(context)
    } else {
        EmptyAliasManager
    }

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val buf = StatementBuffer()
        buf.append(buildUpdateSet(assignments))
        buf.append(" ")
        buf.append(buildWhere())
        return buf.toStatement()
    }

    fun buildUpdateSet(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        return with(StatementBuffer()) {
            val support = BuilderSupport(dialect, aliasManager, this, context.options.escapeSequence)
            append("update ")
            table(context.target, support)
            append(" set ")
            if (assignments.isNotEmpty()) {
                for ((left, right) in assignments) {
                    column(left)
                    append(" = ")
                    operand(right, support)
                    append(", ")
                }
                cutBack(2)
            }
            val version = context.target.versionProperty()
            if (version != null && version !in assignments.map { it.first }) {
                if (assignments.isNotEmpty()) {
                    append(", ")
                }
                column(version)
                append(" = ")
                column(version)
                append(" + 1")
            }
            toStatement()
        }
    }

    fun buildWhere(): Statement {
        return with(StatementBuffer()) {
            val support = BuilderSupport(dialect, aliasManager, this, context.options.escapeSequence)
            val criteria = context.getWhereCriteria()
            if (criteria.isNotEmpty()) {
                append("where ")
                for ((index, criterion) in criteria.withIndex()) {
                    criterion(index, criterion, support)
                    append(" and ")
                }
                cutBack(5)
            }
            toStatement()
        }
    }

    private fun table(expression: TableExpression<*>, support: BuilderSupport) {
        support.visitTableExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    private fun StatementBuffer.column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        append(name)
    }

    private fun criterion(index: Int, c: Criterion, support: BuilderSupport) {
        return support.visitCriterion(index, c)
    }

    private fun operand(operand: Operand, support: BuilderSupport) {
        support.visitOperand(operand)
    }
}
