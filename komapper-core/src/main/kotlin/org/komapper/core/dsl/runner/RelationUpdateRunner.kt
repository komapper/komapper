package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.RelationUpdateStatementBuilder
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.UpdateOptions

class RelationUpdateRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
    @Suppress("unused") private val options: UpdateOptions
) : Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        val clock = config.clockProvider.now()
        val updatedAtAssignment = context.target.updatedAtAssignment(clock)
        return buildStatement(config, updatedAtAssignment)
    }

    fun buildStatement(
        config: DatabaseConfig,
        updatedAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null
    ): Statement {
        checkWhereClause(context, options)
        val builder = RelationUpdateStatementBuilder(config.dialect, context, updatedAtAssignment)
        return builder.build()
    }
}
