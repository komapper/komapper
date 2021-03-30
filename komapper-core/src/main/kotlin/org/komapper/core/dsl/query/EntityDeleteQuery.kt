package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.scope.EntityDeleteOptionsDeclaration
import org.komapper.core.dsl.scope.EntityDeleteOptionsScope
import org.komapper.core.jdbc.JdbcExecutor

interface EntityDeleteQuery<ENTITY> : Query<Unit> {
    fun options(declaration: EntityDeleteOptionsDeclaration): EntityDeleteQuery<ENTITY>
}

internal data class EntityDeleteQueryImpl<ENTITY>(
    private val context: EntityDeleteContext<ENTITY>,
    private val entity: ENTITY
) :
    EntityDeleteQuery<ENTITY> {

    override fun options(declaration: EntityDeleteOptionsDeclaration): EntityDeleteQueryImpl<ENTITY> {
        val scope = EntityDeleteOptionsScope(context.options)
        declaration(scope)
        val newContext = context.copy(options = scope.options)
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config.dialect)
        val executor = JdbcExecutor(config, context.options)
        executor.executeUpdate(statement) { _, count ->
            if (!context.options.ignoreVersion &&
                !context.options.suppressOptimisticLockException &&
                context.entityMetamodel.versionProperty() != null &&
                count != 1
            ) {
                throw OptimisticLockException()
            }
        }
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = EntityDeleteStatementBuilder(dialect, context, entity)
        return builder.build()
    }
}
