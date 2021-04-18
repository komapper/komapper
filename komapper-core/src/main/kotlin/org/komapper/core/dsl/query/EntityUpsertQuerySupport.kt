package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.element.UpdateSet
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.SetScope

internal class EntityUpsertQuerySupport<ENTITY : Any>(
    private val context: EntityUpsertContext<ENTITY>,
    private val insertSupport: EntityInsertQuerySupport<ENTITY>
) {

    fun set(propertyMetamodels: List<PropertyMetamodel<ENTITY, *>>): EntityUpsertContext<ENTITY> {
        return context.copy(updateSet = UpdateSet.Properties(propertyMetamodels))
    }

    fun set(declaration: SetDeclaration<ENTITY>): EntityUpsertContext<ENTITY> {
        val scope = SetScope<ENTITY>().apply(declaration)
        return context.copy(updateSet = UpdateSet.Pairs(scope.toList()))
    }

    fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return insertSupport.preInsert(config, entity)
    }

    fun <T> upsert(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        return insertSupport.insert(config, execute)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entity)
        return builder.build()
    }
}
