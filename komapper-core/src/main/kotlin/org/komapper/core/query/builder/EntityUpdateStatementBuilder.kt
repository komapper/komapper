package org.komapper.core.query.builder

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.EntityUpdateContext

internal class EntityUpdateStatementBuilder<ENTITY>(
    val config: DefaultDatabaseConfig,
    val context: EntityUpdateContext<ENTITY>,
    val entity: ENTITY
) {
    private val aliasManager = AliasManager(context)
    private val buf = StatementBuffer(config.dialect::formatValue)
    private val support = BuilderSupport(config, aliasManager, buf)

    fun build(): Statement {
        val identityProperties = context.entityMetamodel.idProperties()
        val versionProperty = context.entityMetamodel.versionProperty()
        val properties = context.entityMetamodel.properties()
        buf.append("update ")
        buf.append(tableName(context.entityMetamodel))
        buf.append(" set ")
        for (p in properties - identityProperties) {
            buf.append(columnName(p))
            buf.append(" = ")
            val value = Value(p.get(entity), p.klass)
            buf.bind(value)
            // TODO
            if (p === versionProperty) {
                buf.append(" + 1")
            }
            buf.append(", ")
        }
        buf.cutBack(2)
        if (identityProperties.isNotEmpty() || versionProperty != null) {
            buf.append(" where ")
            if (identityProperties.isNotEmpty()) {
                for (p in identityProperties) {
                    buf.append(columnName(p))
                    buf.append(" = ")
                    val value = Value(p.get(entity), p.klass)
                    buf.bind(value)
                    buf.append(" and ")
                }
                if (versionProperty == null) {
                    buf.cutBack(5)
                }
            }
            if (versionProperty != null) {
                buf.append(columnName(versionProperty))
                buf.append(" = ")
                val value = Value(versionProperty.get(entity), versionProperty.klass)
                buf.bind(value)
            }
        }
        return buf.toStatement()
    }

    private fun tableName(entityMetamodel: EntityMetamodel<*>): String {
        return support.tableName(entityMetamodel)
    }

    private fun columnName(propertyMetamodel: PropertyMetamodel<*, *>): String {
        return support.columnName(propertyMetamodel)
    }
}
