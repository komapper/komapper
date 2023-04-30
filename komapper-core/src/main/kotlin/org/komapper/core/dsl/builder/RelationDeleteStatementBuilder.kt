package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface RelationDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun build(): Statement
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> RelationDeleteStatementBuilder(
    dialect: BuilderDialect,
    context: RelationDeleteContext<ENTITY, ID, META>,
): RelationDeleteStatementBuilder<ENTITY, ID, META> {
    return DefaultRelationDeleteStatementBuilder(dialect, context)
}

class DefaultRelationDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: RelationDeleteContext<ENTITY, ID, META>,
) : RelationDeleteStatementBuilder<ENTITY, ID, META> {
    private val aliasManager = if (dialect.supportsAliasForDeleteStatement()) {
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
        val support = BuilderSupport(dialect, aliasManager, buf, context.options.escapeSequence)
        buf.append("delete from ")
        return with(support) {
            table(context.target)
            buf.toStatement()
        }
    }

    fun buildWhere(): Statement {
        val buf = StatementBuffer()
        val support = BuilderSupport(dialect, aliasManager, buf, context.options.escapeSequence)
        val criteria = context.getWhereCriteria()
        return with(support) {
            if (criteria.isNotEmpty()) {
                buf.append("where ")
                for ((index, criterion) in criteria.withIndex()) {
                    criterion(index, criterion)
                    buf.append(" and ")
                }
                buf.cutBack(5)
            }
            buf.toStatement()
        }
    }

    private fun BuilderSupport.table(expression: TableExpression<*>) {
        visitTableExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    private fun BuilderSupport.criterion(index: Int, c: Criterion) {
        visitCriterion(index, c)
    }
}
