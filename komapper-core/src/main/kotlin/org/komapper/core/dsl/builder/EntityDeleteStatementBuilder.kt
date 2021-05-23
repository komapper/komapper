package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.VersionOption

class EntityDeleteStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: EntityDeleteContext<ENTITY, ID, META>,
    val option: VersionOption,
    val entity: ENTITY
) {

    private val aliasManager = DefaultAliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val target = context.target
        val identityProperties = target.idProperties()
        val versionProperty = target.versionProperty()
        val versionRequired = versionProperty != null && !option.ignoreVersion
        buf.append("delete from ")
        table(target)
        if (identityProperties.isNotEmpty() || versionRequired) {
            buf.append(" where ")
            if (identityProperties.isNotEmpty()) {
                for (p in identityProperties) {
                    column(p)
                    buf.append(" = ")
                    buf.bind(p.toValue(entity))
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
                buf.bind(versionProperty.toValue(entity))
            }
        }
        return buf.toStatement()
    }

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        support.visitColumnExpression(expression)
    }
}
