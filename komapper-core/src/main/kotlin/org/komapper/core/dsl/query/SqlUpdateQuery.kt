package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlUpdateStatementBuilder
import org.komapper.core.dsl.command.SqlUpdateCommand
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
import org.komapper.core.metamodel.EntityMetamodel

interface SqlUpdateQuery : Query<Int> {
    fun set(declaration: SetDeclaration): SqlUpdateQuery
    fun where(declaration: WhereDeclaration): SqlUpdateQuery
}

internal data class SqlUpdateQueryImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: SqlUpdateContext<ENTITY> = SqlUpdateContext(entityMetamodel)
) : SqlUpdateQuery {

    override fun set(declaration: SetDeclaration): SqlUpdateQueryImpl<ENTITY> {
        val scope = SetScope()
        declaration(scope)
        val newContext = context.addSet(scope.context.toList())
        return SqlUpdateQueryImpl(entityMetamodel, newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlUpdateQueryImpl<ENTITY> {
        val scope = WhereScope()
        declaration(scope)
        val newContext = context.addWhere(scope.criteria.toList())
        return SqlUpdateQueryImpl(entityMetamodel, newContext)
    }

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config.dialect)
        val command = SqlUpdateCommand(config, statement)
        return command.execute()
    }

    override fun peek(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = SqlUpdateStatementBuilder(dialect, context)
        return builder.build()
    }
}
