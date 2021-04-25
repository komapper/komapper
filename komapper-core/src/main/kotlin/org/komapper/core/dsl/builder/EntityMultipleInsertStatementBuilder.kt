package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface EntityMultipleInsertStatementBuilder<ENTITY : Any> {
    fun build(): Statement
}

internal class EntityMultipleInsertStatementBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: EntityInsertContext<ENTITY, ID, META>,
    val entities: List<ENTITY>
) : EntityMultipleInsertStatementBuilder<ENTITY> {

    private val buf = StatementBuffer(dialect::formatValue)

    override fun build(): Statement {
        val properties = context.target.properties().filter {
            it.idAssignment !is Assignment.AutoIncrement<ENTITY, *>
        }
        buf.append("insert into ")
        buf.append(table(context.target))
        buf.append(" (")
        for (p in properties) {
            buf.append(column(p))
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values ")
        for (entity in entities) {
            buf.append("(")
            for (p in properties) {
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

    private fun table(metamodel: EntityMetamodel<*, *, *>): String {
        return metamodel.getCanonicalTableName(dialect::enquote)
    }

    private fun column(expression: ColumnExpression<*>): String {
        return expression.getCanonicalColumnName(dialect::enquote)
    }
}
