package org.komapper.core.query.builder

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo
import org.komapper.core.query.context.EntityDeleteContext

internal class EntityDeleteStatementBuilder<ENTITY>(
    val config: DatabaseConfig,
    val context: EntityDeleteContext<ENTITY>,
    val entity: ENTITY
) {
    private val aliasManager = AliasManager(context)
    private val buf = StatementBuffer(config.dialect::formatValue)
    private val support = BuilderSupport(config, aliasManager, buf)

    fun build(): Statement {
        buf.append("delete from ")
        buf.append(tableName(context.entityMetamodel))
        val identityProperties = context.entityMetamodel.idProperties()
        val versionProperty = context.entityMetamodel.versionProperty()
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

    private fun tableName(tableInfo: TableInfo): String {
        return support.tableName(tableInfo)
    }

    private fun columnName(columnInfo: ColumnInfo<*>): String {
        return support.columnName(columnInfo)
    }
}
