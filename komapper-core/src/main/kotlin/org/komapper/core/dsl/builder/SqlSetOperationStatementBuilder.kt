package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationComponent
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind

internal class SqlSetOperationStatementBuilder(
    private val dialect: Dialect,
    private val context: SqlSetOperationContext<*>
) {

    private val buf = StatementBuffer(dialect::formatValue)
    private val aliasManager = EmptyAliasManager()
    private val support = OrderByBuilderSupport(dialect, context.orderBy, aliasManager, buf)

    fun build(): Statement {
        visitSetOperationComponent(context.component)
        support.orderByClause()
        return buf.toStatement()
    }

    private fun visitSetOperationComponent(component: SqlSetOperationComponent<*>) {
        when (component) {
            is SqlSetOperationComponent.Leaf -> visitSelectContext(component.context)
            is SqlSetOperationComponent.Composite -> {
                visitSetOperationComponent(component.left)
                val operator = when (component.kind) {
                    SqlSetOperationKind.INTERSECT -> "intersect"
                    SqlSetOperationKind.EXCEPT -> "except"
                    SqlSetOperationKind.UNION -> "union"
                    SqlSetOperationKind.UNION_ALL -> "union all"
                }
                buf.append(" $operator ")
                visitSetOperationComponent(component.right)
            }
        }
    }

    private fun visitSelectContext(selectContext: SqlSelectContext<*>) {
        val builder = SqlSelectStatementBuilder(dialect, selectContext)
        val statement = builder.build()
        buf.append("(")
        buf.append(statement)
        buf.append(")")
    }
}
