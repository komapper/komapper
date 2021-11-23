package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.RelationInsertStatementBuilder
import org.komapper.core.dsl.context.RelationInsertContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.InsertOptions

class RelationInsertRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertContext<ENTITY, ID, META>,
    @Suppress("unused") private val options: InsertOptions = InsertOptions.default
) : Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig, idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>? = null): Statement {
        val builder = RelationInsertStatementBuilder(config.dialect, context, idAssignment)
        return builder.build()
    }
}
