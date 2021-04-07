package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.Assignment

internal class EntityInsertStatementBuilder<ENTITY : Any>(
    val dialect: Dialect,
    val context: EntityInsertContext<ENTITY>,
    val entity: ENTITY
) {
    private val buf = StatementBuffer(dialect::formatValue)

    fun build(): Statement {
        val entityMetamodel = context.entityMetamodel
        val properties = entityMetamodel.properties()
        buf.append("insert into ")
        buf.append(table(entityMetamodel))
        buf.append(" (")
        for (p in properties) {
            buf.append(column(p))
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values (")
        for (p in properties) {
            val value = if (p in entityMetamodel.idProperties() &&
                entityMetamodel.idAssignment() is Assignment.Identity<ENTITY, *>
            ) {
                Value(null, p.klass)
            } else {
                Value(p.getter(entity), p.klass)
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
