package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.isAutoIncrement

class SqlInsertStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: SqlInsertContext<ENTITY, ID, META>
) {
    private val aliasManager = DefaultAliasManager(context)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val target = context.target
        buf.append("insert into ")
        table(target)
        when (val values = context.values) {
            is Values.Pairs -> {
                buf.append(" (")
                for (property in values.pairs.map { it.first }) {
                    if (property.isAutoIncrement()) {
                        continue
                    }
                    column(property)
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append(") values (")
                for ((property, operand) in values.pairs) {
                    if (property.isAutoIncrement()) {
                        continue
                    }
                    operand(operand)
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append(")")
            }
            is Values.Subquery -> {
                buf.append(" (")
                for (p in target.properties()) {
                    column(p)
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append(") ")
                subquery(values.expression)
            }
        }
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

    private fun subquery(expression: SubqueryExpression<*>) {
        val statement = support.buildSubqueryStatement(expression)
        buf.append(statement)
    }
}
