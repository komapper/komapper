package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.expr.EntityExpression
import org.komapper.core.dsl.expr.PropertyExpression
import org.komapper.core.dsl.query.EntityDeleteOption

internal class EntityDeleteStatementBuilder<ENTITY : Any>(
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
        table(context.entityMetamodel)
        val identityProperties = context.entityMetamodel.idProperties()
        val versionProperty = context.entityMetamodel.versionProperty()
        val versionRequired = versionProperty != null && !option.ignoreVersion
        if (identityProperties.isNotEmpty() || versionRequired) {
            buf.append(" where ")
            if (identityProperties.isNotEmpty()) {
                for (p in identityProperties) {
                    column(p)
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
                column(versionProperty)
                buf.append(" = ")
                val value = Value(versionProperty.getter(entity), versionProperty.klass)
                buf.bind(value)
            }
        }
        return buf.toStatement()
    }

    private fun table(expression: EntityExpression<*>) {
        support.visitEntityExpression(expression)
    }

    private fun column(expression: PropertyExpression<*>) {
        support.visitPropertyExpression(expression)
    }
}
