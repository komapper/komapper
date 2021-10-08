package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext

class SqlSetOperationStatementBuilder(
    private val dialect: Dialect,
    private val context: SqlSetOperationContext,
    private val aliasManager: AliasManager
) {

    private val buf = StatementBuffer()
    private val support = OrderByBuilderSupport(dialect, context.orderBy, EmptyAliasManager, buf)

    fun build(): Statement {
        visitSubqueryContext(context)
        support.orderByClause()
        return buf.toStatement()
    }

    private fun visitSubqueryContext(subqueryContext: SubqueryContext) {
        when (subqueryContext) {
            is EntitySelectContext<*, *, *> -> visitEntityContext(subqueryContext)
            is SqlSelectContext<*, *, *> -> visitSelectContext(subqueryContext)
            is SqlSetOperationContext -> {
                visitSubqueryContext(subqueryContext.left)
                val operator = when (subqueryContext.kind) {
                    SqlSetOperationKind.INTERSECT -> "intersect"
                    SqlSetOperationKind.EXCEPT -> "except"
                    SqlSetOperationKind.UNION -> "union"
                    SqlSetOperationKind.UNION_ALL -> "union all"
                }
                buf.append(" $operator ")
                visitSubqueryContext(subqueryContext.right)
            }
        }
    }

    private fun visitEntityContext(selectContext: EntitySelectContext<*, *, *>) {
        val childAliasManager = DefaultAliasManager(selectContext, aliasManager)
        val builder = EntitySelectStatementBuilder(dialect, selectContext, childAliasManager)
        val statement = builder.build()
        buf.append("(")
        buf.append(statement)
        buf.append(")")
    }

    private fun visitSelectContext(selectContext: SqlSelectContext<*, *, *>) {
        val childAliasManager = DefaultAliasManager(selectContext, aliasManager)
        val builder = SqlSelectStatementBuilder(dialect, selectContext, childAliasManager)
        val statement = builder.build()
        buf.append("(")
        buf.append(statement)
        buf.append(")")
    }
}
