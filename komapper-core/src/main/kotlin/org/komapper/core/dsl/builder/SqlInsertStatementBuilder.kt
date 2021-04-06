package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.getName
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.Column
import org.komapper.core.metamodel.Table

internal class SqlInsertStatementBuilder<ENTITY>(
    val dialect: Dialect,
    val context: SqlInsertContext<ENTITY>
) {
    private val buf = StatementBuffer(dialect::formatValue)

    fun build(): Statement {
        val entityMetamodel = context.entityMetamodel
        buf.append("insert into ")
        buf.append(tableName(entityMetamodel))
        buf.append(" (")
        for (column in context.values.map { it.first }) {
            buf.append(columnName(column.column))
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values (")
        for (parameter in context.values.map { it.second }) {
            val value = if (parameter.column in entityMetamodel.idProperties() &&
                entityMetamodel.idAssignment() is Assignment.Identity<ENTITY, *>
            ) {
                Value(null, parameter.column.klass)
            } else {
                Value(parameter.value, parameter.column.klass)
            }
            buf.bind(value)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        return buf.toStatement()
    }

    private fun tableName(table: Table): String {
        return table.getName(dialect::quote)
    }

    private fun columnName(column: Column<*>): String {
        return column.getName(dialect::quote)
    }
}
