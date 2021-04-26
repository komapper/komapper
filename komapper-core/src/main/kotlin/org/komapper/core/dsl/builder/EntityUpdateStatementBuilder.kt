package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.Value
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.VersionOption

internal class EntityUpdateStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: EntityUpdateContext<ENTITY, ID, META>,
    val option: VersionOption,
    val entity: ENTITY
) {
    private val aliasManager = DefaultAliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val target = context.target
        val idProperties = target.idProperties()
        val versionProperty = target.versionProperty()
        buf.append("update ")
        table(context.target)
        buf.append(" set ")
        for (p in context.getTargetProperties()) {
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

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_ONLY)
    }

    private fun column(expression: ColumnExpression<*>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }
}
