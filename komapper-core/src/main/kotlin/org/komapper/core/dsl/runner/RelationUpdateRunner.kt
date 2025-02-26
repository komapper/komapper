package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.getAssignments
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class RelationUpdateRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
) : Runner {
    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val clock = config.clockProvider.now()
        val updatedAtAssignment = context.target.updatedAtAssignment(clock)
        val result = buildStatement(config, updatedAtAssignment)
        val statement = result.getOrThrow()
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(
        config: DatabaseConfig,
        updatedAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null,
    ): Result<Statement> {
        checkWhereClause(context)
        val assignments = getAssignments(updatedAtAssignment)
        if (assignments.isEmpty() && context.target.versionProperty() == null) {
            return Result.failure(
                IllegalStateException("No update statement is generated because no assignment is specified."),
            )
        }
        val builder = config.dialect.getRelationUpdateStatementBuilder(BuilderDialect(config), context)
        val statement = builder.build(assignments)
        return Result.success(statement)
    }

    private fun getAssignments(updatedAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
        val assignments = context.getAssignments()
        val properties = assignments.map { it.first }
        val additionalAssignment = listOfNotNull(updatedAtAssignment).filterNot { it.first in properties }
        return assignments + additionalAssignment
    }
}
