package org.komapper.core.query.builder

import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.jdbc.Dialect
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo
import org.komapper.core.query.context.EntityInsertContext

internal class EntityInsertStatementBuilder<ENTITY>(
    val dialect: Dialect,
    val context: EntityInsertContext<ENTITY>,
    val entity: ENTITY
) {
    private val buf = StatementBuffer(dialect::formatValue)

    fun build(): Statement {
        val entityMetamodel = context.entityMetamodel
        val properties = entityMetamodel.properties()
        buf.append("insert into ")
        buf.append(tableName(entityMetamodel))
        buf.append(" (")
        for (p in properties) {
            buf.append(columnName(p))
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
                Value(p.get(entity), p.klass)
            }
            buf.bind(value)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        return buf.toStatement()
    }

    private fun tableName(tableInfo: TableInfo): String {
        return tableInfo.tableName()
    }

    private fun columnName(columnInfo: ColumnInfo<*>): String {
        return columnInfo.columnName
    }
}
