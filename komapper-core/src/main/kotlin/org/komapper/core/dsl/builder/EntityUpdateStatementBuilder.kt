package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo

internal class EntityUpdateStatementBuilder<ENTITY>(
    val dialect: Dialect,
    val context: EntityUpdateContext<ENTITY>,
    val entity: ENTITY
) {
    private val aliasManager = AliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

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
            val value = Value(p.getter(entity), p.klass)
            buf.bind(value)
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
                    val value = Value(p.getter(entity), p.klass)
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
                val value = Value(versionProperty.getter(entity), versionProperty.klass)
                buf.bind(value)
            }
        }
        return buf.toStatement()
    }

    private fun tableName(tableInfo: TableInfo): String {
        return support.tableName(tableInfo)
    }

    private fun columnName(columnInfo: ColumnInfo<*>): String {
        return support.columnName(columnInfo)
    }
}
