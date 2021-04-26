package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class SqlDeleteStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: SqlDeleteContext<ENTITY, ID, META>
) {
    private val aliasManager = DefaultAliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        buf.append("delete from ")
        table(context.target)
        if (context.where.isNotEmpty()) {
            buf.append(" where ")
            for ((index, criterion) in context.where.withIndex()) {
                criterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        return buf.toStatement()
    }

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    private fun criterion(index: Int, c: Criterion) {
        return support.visitCriterion(index, c)
    }
}
