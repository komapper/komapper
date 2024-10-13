package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

interface RelationInsertValuesStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> RelationInsertValuesStatementBuilder(
    dialect: BuilderDialect,
    context: RelationInsertValuesContext<ENTITY, ID, META>,
): RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
    return DefaultRelationInsertValuesStatementBuilder(dialect, context)
}

class DefaultRelationInsertValuesStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
    private val aliasManager = DefaultAliasManager(context)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val buf = StatementBuffer()
        buf.append(buildInsertInto(assignments))
        buf.append(" ")
        buf.append(buildValues(assignments))
        return buf.toStatement()
    }

    fun buildInsertInto(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val target = context.target
        return with(StatementBuffer()) {
            append("insert into ")
            table(target)
            append(" (")
            if (assignments.isNotEmpty()) {
                for ((property, _) in assignments) {
                    column(property)
                    append(", ")
                }
            }
            cutBack(2)
            append(")")
            toStatement()
        }
    }

    fun buildValues(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        return with(StatementBuffer()) {
            val support = BuilderSupport(dialect, aliasManager, this)
            append("values (")
            if (assignments.isNotEmpty()) {
                for ((_, operand) in assignments) {
                    operand(operand, support)
                    append(", ")
                }
            }
            cutBack(2)
            append(")")
            toStatement()
        }
    }

    private fun StatementBuffer.table(metamodel: EntityMetamodel<*, *, *>) {
        val name = metamodel.getCanonicalTableName(dialect::enquote)
        append(name)
    }

    private fun StatementBuffer.column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        append(name)
    }

    private fun operand(operand: Operand, support: BuilderSupport) {
        support.visitOperand(operand)
    }
}
