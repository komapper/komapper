package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.scope.EntityUpdateOptionsDeclaration
import org.komapper.core.dsl.scope.EntityUpdateOptionsScope
import org.komapper.core.jdbc.JdbcExecutor

internal interface EntityUpdateQuery<ENTITY> : Query<ENTITY> {
    fun options(declaration: EntityUpdateOptionsDeclaration): EntityUpdateQuery<ENTITY>
}

internal data class EntityUpdateQueryImpl<ENTITY>(
    private val context: EntityUpdateContext<ENTITY>,
    private val entity: ENTITY
) :
    EntityUpdateQuery<ENTITY> {

    override fun options(declaration: EntityUpdateOptionsDeclaration): EntityUpdateQuery<ENTITY> {
        val scope = EntityUpdateOptionsScope(context.options)
        declaration(scope)
        val newContext = context.copy(options = scope.options)
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): ENTITY {
        val clock = config.clockProvider.now()
        val newEntity = context.entityMetamodel.updateUpdatedAt(entity, clock)
        val statement = buildStatement(config.dialect, newEntity)
        val executor = JdbcExecutor(config, context.options)
        return executor.executeUpdate(statement) { _, count ->
            if (!context.options.ignoreVersion &&
                !context.options.suppressOptimisticLockException &&
                context.entityMetamodel.versionProperty() != null &&
                count != 1
            ) {
                throw OptimisticLockException()
            }
            if (!context.options.ignoreVersion) {
                context.entityMetamodel.incrementVersion(newEntity)
            } else {
                newEntity
            }
        }
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect, entity)
    }

    private fun buildStatement(dialect: Dialect, entity: ENTITY): Statement {
        val builder = EntityUpdateStatementBuilder(dialect, context, entity)
        return builder.build()
    }
}
