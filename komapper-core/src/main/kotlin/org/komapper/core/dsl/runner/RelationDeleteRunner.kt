package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class RelationDeleteRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
) : Runner {
    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statement = buildStatement(config)
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        checkWhereClause(context)
        val builder = config.dialect.getRelationDeleteStatementBuilder(BuilderDialect(config), context)
        return builder.build()
    }
}
