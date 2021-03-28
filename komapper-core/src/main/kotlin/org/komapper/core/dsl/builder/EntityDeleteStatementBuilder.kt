package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo

internal class EntityDeleteStatementBuilder<ENTITY>(
    val dialect: Dialect,
    val context: EntityDeleteContext<ENTITY>,
    val entity: ENTITY
) {
    private val aliasManager = AliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        buf.append("delete from ")
        visitTableInfo(context.entityMetamodel)
        val identityProperties = context.entityMetamodel.idProperties()
        val versionProperty = context.entityMetamodel.versionProperty()
        if (identityProperties.isNotEmpty() || versionProperty != null) {
            buf.append(" where ")
            if (identityProperties.isNotEmpty()) {
                for (p in identityProperties) {
                    visitColumnInfo(p)
                    buf.append(" = ")
                    val value = Value(p.getter(entity), p.klass)
                    buf.bind(value)
                    buf.append(" and ")
                }
            }
            if (versionProperty != null) {
                visitColumnInfo(versionProperty)
                buf.append(" = ")
                val value = Value(versionProperty.getter(entity), versionProperty.klass)
                buf.bind(value)
            }
        }
        return buf.toStatement()
    }

    private fun visitTableInfo(tableInfo: TableInfo) {
        support.visitTableInfo(tableInfo)
    }

    private fun visitColumnInfo(columnInfo: ColumnInfo<*>) {
        support.visitColumnInfo(columnInfo)
    }
}
