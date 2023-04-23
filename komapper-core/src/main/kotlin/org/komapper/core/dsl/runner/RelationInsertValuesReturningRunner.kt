package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class RelationInsertValuesReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: RelationInsertValuesContext<ENTITY, ID, META>,
) : Runner {

    private val runner: RelationInsertValuesRunner<ENTITY, ID, META> = RelationInsertValuesRunner(context)

    override fun check(config: DatabaseConfig) {
        checkInsertReturning(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }

    fun buildStatement(
        config: DatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>?,
    ): Statement {
        return runner.buildStatement(config, idAssignment)
    }

    fun preInsertUsingSequence(idGenerator: IdGenerator.Sequence<ENTITY, ID>, id: ID): Pair<PropertyMetamodel<ENTITY, ID, *>, Operand> {
        return runner.preInsertUsingSequence(idGenerator, id)
    }
}
