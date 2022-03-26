package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.RelationInsertSelectStatementBuilder
import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class RelationInsertSelectRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertSelectContext<ENTITY, ID, META>,
) : Runner {

    override fun check(config: DatabaseConfig) {
        checkAutoIncrementWhenInsertingMultipleRows(config, context.target)
        checkGeneratedKeysReturningWhenInsertingMultipleRows(config, context.target, context.options)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statement = buildStatement(config)
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val builder = RelationInsertSelectStatementBuilder(BuilderDialect(config), context)
        return builder.build()
    }
}
