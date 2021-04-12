package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.option.VersionOption

internal class EntityUpdateStatementBuilder<ENTITY : Any>(
    val dialect: Dialect,
    val context: EntityUpdateContext<ENTITY>,
    val entity: ENTITY,
    val option: VersionOption
) {
    private val aliasManager = AliasManagerImpl(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val idProperties = context.entityMetamodel.idProperties()
        val versionProperty = context.entityMetamodel.versionProperty()
        val createdAtProperty = context.entityMetamodel.createdAtProperty()
        val properties = context.entityMetamodel.properties()
        buf.append("update ")
        table(context.entityMetamodel)
        buf.append(" set ")
        for (p in (properties - idProperties).filter { it != createdAtProperty }) {
            column(p)
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
        if (idProperties.isNotEmpty() || versionRequired) {
            buf.append(" where ")
            if (idProperties.isNotEmpty()) {
                for (p in idProperties) {
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
