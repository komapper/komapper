package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface EntityDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun build(): Statement
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityDeleteStatementBuilder(
    dialect: BuilderDialect,
    context: EntityDeleteContext<ENTITY, ID, META>,
    entity: ENTITY,
): EntityDeleteStatementBuilder<ENTITY, ID, META> {
    return DefaultEntityDeleteStatementBuilder(dialect, context, entity)
}

class DefaultEntityDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityDeleteStatementBuilder<ENTITY, ID, META> {

    private val aliasManager =
        if (dialect.supportsAliasForDeleteStatement()) {
            DefaultAliasManager(context)
        } else {
            EmptyAliasManager
        }

    override fun build(): Statement {
        val buf = StatementBuffer()
        buf.append(buildDeleteFrom())
        buf.appendIfNotEmpty(buildWhere())
        return buf.toStatement()
    }

    fun buildDeleteFrom(): Statement {
        val buf = StatementBuffer()
        val support = BuilderSupport(dialect, aliasManager, buf)
        return with(support) {
            buf.append("delete from ")
            table(context.target)
            buf.toStatement()
        }
    }

    fun buildWhere(): Statement {
        val buf = StatementBuffer()
        val support = BuilderSupport(dialect, aliasManager, buf)
        val target = context.target
        val identityProperties = target.idProperties()
        val versionProperty = target.versionProperty()
        val versionRequired = versionProperty != null && !context.options.disableOptimisticLock
        val criteria = context.getWhereCriteria()
        return with(support) {
            if (criteria.isNotEmpty() || identityProperties.isNotEmpty() || versionRequired) {
                buf.append("where ")
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
            buf.toStatement()
        }
    }

    private fun BuilderSupport.table(expression: TableExpression<*>) {
        visitTableExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    private fun BuilderSupport.column(expression: ColumnExpression<*, *>) {
        visitColumnExpression(expression)
    }

    private fun BuilderSupport.criterion(index: Int, c: Criterion) {
        visitCriterion(index, c)
    }
}
