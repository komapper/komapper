package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SchemaContext

class SchemaDropRunner(
    private val context: SchemaContext,
) : Runner {
    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statements = buildStatements(config)
        return DryRunStatement.of(statements, config)
    }

    fun buildStatements(config: DatabaseConfig): List<Statement> {
        val builder = config.dialect.getSchemaStatementBuilder(BuilderDialect(config))
        return builder.drop(context.metamodels)
    }
}
