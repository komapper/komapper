package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.query.EntityUpdateOption
import org.komapper.core.metamodel.Column
import org.komapper.core.metamodel.Table

internal class EntityUpdateStatementBuilder<ENTITY>(
    val dialect: Dialect,
    val context: EntityUpdateContext<ENTITY>,
    val entity: ENTITY,
    val option: EntityUpdateOption
) {
    private val aliasManager = AliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val identityProperties = context.entityMetamodel.idProperties()
        val versionProperty = context.entityMetamodel.versionProperty()
        val properties = context.entityMetamodel.properties()
        buf.append("update ")
        visitTable(context.entityMetamodel)
        buf.append(" set ")
        for (p in properties - identityProperties) {
            visitColumn(p)
            buf.append(" = ")
            val value = Value(p.getter(entity), p.klass)
            buf.bind(value)
            if (p == versionProperty) {
                buf.append(" + 1")
            }
            buf.append(", ")
        }
        buf.cutBack(2)
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
