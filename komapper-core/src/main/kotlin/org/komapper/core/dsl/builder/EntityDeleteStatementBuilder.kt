package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.query.EntityDeleteOption
import org.komapper.core.metamodel.Column
import org.komapper.core.metamodel.Table

internal class EntityDeleteStatementBuilder<ENTITY>(
    val dialect: Dialect,
    val context: EntityDeleteContext<ENTITY>,
    val entity: ENTITY,
    val option: EntityDeleteOption
) {
    private val aliasManager = AliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        buf.append("delete from ")
        visitTable(context.entityMetamodel)
        val identityProperties = context.entityMetamodel.idProperties()
        val versionProperty = context.entityMetamodel.versionProperty()
        val versionRequired = versionProperty != null && !option.ignoreVersion
        if (identityProperties.isNotEmpty() || versionRequired) {
            buf.append(" where ")
            if (identityProperties.isNotEmpty()) {
                for (p in identityProperties) {
                    visitColumn(p)
                    buf.append(" = ")
                    val value = Value(p.getter(entity), p.klass)
                    buf.bind(value)
                    buf.append(" and ")
                }
                if (!versionRequired) {
                    buf.cutBack(5)
                }
            }
            if (versionRequired) {
                checkNotNull(versionProperty)
                visitColumn(versionProperty)
                buf.append(" = ")
                val value = Value(versionProperty.getter(entity), versionProperty.klass)
                buf.bind(value)
            }
        }
        return buf.toStatement()
    }

    private fun visitTable(table: Table) {
        support.visitTable(table)
    }

    private fun visitColumn(column: Column<*>) {
        support.visitColumn(column)
    }
}
