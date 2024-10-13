package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class RelationUpdateReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: RelationUpdateContext<ENTITY, ID, META>,
) : Runner {
    val runner: RelationUpdateRunner<ENTITY, ID, META> = RelationUpdateRunner(context)

    override fun check(config: DatabaseConfig) {
        checkUpdateReturning(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }

    fun buildStatement(
        config: DatabaseConfig,
        updatedAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null,
    ): Result<Statement> {
        return runner.buildStatement(config, updatedAtAssignment)
    }
}
