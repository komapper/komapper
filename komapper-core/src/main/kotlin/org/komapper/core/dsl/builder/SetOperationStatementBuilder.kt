package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression

class SetOperationStatementBuilder(
    private val dialect: BuilderDialect,
    private val context: SetOperationContext,
    private val aliasManager: AliasManager,
    private val projectionPredicate: (ColumnExpression<*, *>) -> Boolean = { true },
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
            is SelectContext<*, *, *> -> {
                visitSelectContext(subqueryContext)
            }

            is SetOperationContext -> {
                visitSubqueryContext(subqueryContext.left)
                val operator = when (subqueryContext.kind) {
                    SetOperationKind.INTERSECT -> {
                        if (dialect.supportsSetOperationIntersect()) {
                            "intersect"
                        } else {
                            throw UnsupportedOperationException(
                                "The dialect(driver=${dialect.driver}) does not support the \"intersect\" set operation."
                            )
                        }
                    }

                    SetOperationKind.EXCEPT -> {
                        if (dialect.supportsSetOperationExcept()) {
                            "except"
                        } else if (dialect.supportsSetOperationMinus()) {
                            "minus"
                        } else {
                            throw UnsupportedOperationException(
                                "The dialect(driver=${dialect.driver}) does not support the \"except\" and \"minus\" set operations."
                            )
                        }
                    }

                    SetOperationKind.UNION -> {
                        "union"
                    }

                    SetOperationKind.UNION_ALL -> {
                        "union all"
                    }
                }
                buf.append(" $operator ")
                visitSubqueryContext(subqueryContext.right)
            }
        }
    }

    private fun visitSelectContext(selectContext: SelectContext<*, *, *>) {
        val childAliasManager = DefaultAliasManager(selectContext, aliasManager)
        val builder = SelectStatementBuilder(dialect, selectContext, childAliasManager, projectionPredicate)
        val statement = builder.build()
        buf.append("(")
        buf.append(statement)
        buf.append(")")
    }

    private fun raiseException(keyword: String): Nothing {
        throw UnsupportedOperationException("The dialect(driver=${dialect.driver}) does not support the \"$keyword\" set operation.")
    }
}
