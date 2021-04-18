package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface EntityMultiInsertStatementBuilder<ENTITY : Any> {
    fun build(): Statement
}

internal class EntityMultiInsertStatementBuilderImpl<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    val dialect: Dialect,
    val context: EntityInsertContext<ENTITY, META>,
    val entities: List<ENTITY>
) : EntityMultiInsertStatementBuilder<ENTITY> {

    private val buf = StatementBuffer(dialect::formatValue)

    override fun build(): Statement {
        val entityMetamodel = context.target
        val properties = entityMetamodel.properties()
        buf.append("insert into ")
        buf.append(table(entityMetamodel))
        buf.append(" (")
        for (p in properties.filter { it.idAssignment !is Assignment.Identity<ENTITY, *> }) {
            buf.append(column(p))
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values ")
        for (entity in entities) {
            buf.append("(")
            for (p in properties.filter { it.idAssignment !is Assignment.Identity<ENTITY, *> }) {
                val value = Value(p.getter(entity), p.klass)
                buf.bind(value)
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append("), ")
        }
        buf.cutBack(2)
        return buf.toStatement()
    }

    private fun table(expression: EntityExpression<*>): String {
        return expression.getCanonicalTableName(dialect::enquote)
    }

    private fun column(expression: PropertyExpression<*>): String {
        return expression.getCanonicalColumnName(dialect::enquote)
    }
}
