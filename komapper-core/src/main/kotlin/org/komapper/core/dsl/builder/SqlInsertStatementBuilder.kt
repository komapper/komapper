package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.Assignment

internal class SqlInsertStatementBuilder<ENTITY : Any>(
    val dialect: Dialect,
    val context: SqlInsertContext<ENTITY>
) {
    private val buf = StatementBuffer(dialect::formatValue)

    fun build(): Statement {
        val entityMetamodel = context.entityMetamodel
        buf.append("insert into ")
        buf.append(table(entityMetamodel))
        buf.append(" (")
        for (column in context.values.map { it.first }) {
            buf.append(column(column.expression))
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values (")
        for (parameter in context.values.map { it.second }) {
            val value = if (parameter.expression in entityMetamodel.idProperties() &&
                entityMetamodel.idAssignment() is Assignment.Identity<ENTITY, *>
            ) {
                Value(null, parameter.expression.klass)
            } else {
                Value(parameter.value, parameter.expression.klass)
            }
            buf.bind(value)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        return buf.toStatement()
    }

    private fun table(expression: EntityExpression<*>): String {
        return expression.getCanonicalTableName(dialect::quote)
    }

    private fun column(expression: PropertyExpression<*>): String {
        return expression.getCanonicalColumnName(dialect::quote)
    }
}
