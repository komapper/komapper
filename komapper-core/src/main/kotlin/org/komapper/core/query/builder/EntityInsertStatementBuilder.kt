package org.komapper.core.query.builder

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.EntityInsertContext

internal class EntityInsertStatementBuilder<ENTITY>(
    val config: DatabaseConfig,
    val context: EntityInsertContext<ENTITY>,
    val entity: ENTITY
) {
    private val buf = StatementBuffer(config.dialect::formatValue)

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

    private fun tableName(entityMetamodel: EntityMetamodel<*>): String {
        return entityMetamodel.tableName()
    }

    private fun columnName(propertyMetamodel: PropertyMetamodel<*, *>): String {
        return propertyMetamodel.columnName
    }
}
