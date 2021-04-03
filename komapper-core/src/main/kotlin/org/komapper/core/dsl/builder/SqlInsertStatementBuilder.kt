package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.getName
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo

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
            buf.append(columnName(column.columnInfo))
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values (")
        for (parameter in context.values.map { it.second }) {
            val value = if (parameter.columnInfo in entityMetamodel.idProperties() &&
                entityMetamodel.idAssignment() is Assignment.Identity<ENTITY, *>
            ) {
                Value(null, parameter.columnInfo.klass)
            } else {
                Value(parameter.value, parameter.columnInfo.klass)
            }
            buf.bind(value)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        return buf.toStatement()
    }

    private fun tableName(tableInfo: TableInfo): String {
        return tableInfo.getName(dialect::quote)
    }

    private fun columnName(columnInfo: ColumnInfo<*>): String {
        return columnInfo.getName(dialect::quote)
    }
}
