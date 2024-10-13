package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.DefaultAliasManager
import org.komapper.core.dsl.builder.SetOperationStatementBuilder
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SubqueryContext

class SetOperationRunner(
    private val context: SetOperationContext,
) : Runner {
    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statement = buildStatement(config)
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        checkWhereClauses(context.left)
        checkWhereClauses(context.right)
        val aliasManager = DefaultAliasManager(context)
        val builder = SetOperationStatementBuilder(BuilderDialect(config), context, aliasManager)
        return builder.build()
    }

    private fun checkWhereClauses(subqueryContext: SubqueryContext) {
        when (subqueryContext) {
            is SelectContext<*, *, *> -> {
                checkWhereClause(subqueryContext)
            }
            is SetOperationContext -> {
                checkWhereClauses(subqueryContext.left)
                checkWhereClauses(subqueryContext.right)
            }
        }
    }
}
