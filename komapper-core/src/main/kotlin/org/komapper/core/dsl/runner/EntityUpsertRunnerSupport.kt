package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.createAssignments
import org.komapper.core.dsl.builder.getAssignments
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityUpsertRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
) {

    private val support: EntityInsertRunnerSupport<ENTITY, ID, META> = EntityInsertRunnerSupport(context.insertContext)

    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val assignments = context.getAssignments().ifEmpty { context.createAssignments() }
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entities)
        return builder.build(assignments)
    }

    fun postInsert(entity: ENTITY, generatedKey: Long): ENTITY {
        return support.postInsert(entity, generatedKey)
    }
}
