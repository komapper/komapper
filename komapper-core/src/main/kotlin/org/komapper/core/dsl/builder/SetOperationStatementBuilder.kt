package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SetOperationKind
import org.komapper.core.dsl.context.SubqueryContext

class SetOperationStatementBuilder(
    private val dialect: Dialect,
    private val context: SetOperationContext,
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
            is SelectContext<*, *, *> -> visitSelectContext(subqueryContext)
            is SetOperationContext -> {
                visitSubqueryContext(subqueryContext.left)
                val operator = when (subqueryContext.kind) {
                    SetOperationKind.INTERSECT -> "intersect"
                    SetOperationKind.EXCEPT -> "except"
                    SetOperationKind.UNION -> "union"
                    SetOperationKind.UNION_ALL -> "union all"
                }
                buf.append(" $operator ")
                visitSubqueryContext(subqueryContext.right)
            }
        }
    }

    private fun visitSelectContext(selectContext: SelectContext<*, *, *>) {
        val childAliasManager = DefaultAliasManager(selectContext, aliasManager)
        val builder = SelectStatementBuilder(dialect, selectContext, childAliasManager)
        val statement = builder.build()
        buf.append("(")
        buf.append(statement)
        buf.append(")")
    }
}
