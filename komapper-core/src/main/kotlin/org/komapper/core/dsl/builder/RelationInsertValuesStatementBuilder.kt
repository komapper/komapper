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

internal class DefaultRelationInsertValuesStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
    private val aliasManager = DefaultAliasManager(context)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val target = context.target
        buf.append("insert into ")
        table(target)
        buf.append(" (")
        if (assignments.isNotEmpty()) {
            for ((property, _) in assignments) {
                column(property)
                buf.append(", ")
            }
        }
        buf.cutBack(2)
        buf.append(") values (")
        if (assignments.isNotEmpty()) {
            for ((_, operand) in assignments) {
                operand(operand)
                buf.append(", ")
            }
        }
        buf.cutBack(2)
        buf.append(")")
        return buf.toStatement()
    }

    private fun table(metamodel: EntityMetamodel<*, *, *>) {
        val name = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append(name)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }
}
