package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) {

    private val aliasManager =
        if (dialect.supportsAliasForDeleteStatement()) {
            DefaultAliasManager(context)
        } else {
            EmptyAliasManager
        }
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val target = context.target
        val identityProperties = target.idProperties()
        val versionProperty = target.versionProperty()
        val versionRequired = versionProperty != null && !context.options.disableOptimisticLock
        buf.append("delete from ")
        table(target)
        val criteria = context.getWhereCriteria()
        if (criteria.isNotEmpty() || identityProperties.isNotEmpty() || versionRequired) {
            buf.append(" where ")
            for ((index, criterion) in criteria.withIndex()) {
                criterion(index, criterion)
                buf.append(" and ")
            }
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

    private fun criterion(index: Int, c: Criterion) {
        support.visitCriterion(index, c)
    }
}
