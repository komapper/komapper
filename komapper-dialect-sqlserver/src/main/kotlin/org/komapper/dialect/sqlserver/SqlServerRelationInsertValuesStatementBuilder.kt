package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.DefaultAliasManager
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class SqlServerRelationInsertValuesStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
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
        buf.append(")")
        val outputExpressions = context.returning.expressions()
        if (outputExpressions.isNotEmpty()) {
            buf.append(" output ")
            for (e in outputExpressions) {
                buf.append("inserted.")
                column(e)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        buf.append(" values (")
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
