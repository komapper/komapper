package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.SetScope

internal class EntityUpsertQuerySupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val insertSupport: EntityInsertQuerySupport<ENTITY, ID, META>
) {

    fun set(declaration: SetScope<ENTITY>.(META) -> Unit): EntityUpsertContext<ENTITY, ID, META> {
        val scope = SetScope<ENTITY>()
        declaration(scope, context.excluded)
        return context.copy(assignmentMap = scope.toMap())
    }

    fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return insertSupport.preInsert(config, entity)
    }

    fun <T> upsert(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, insertSupport.option)
        return execute(executor)
    }
}
