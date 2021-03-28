package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlInsertStatementBuilder
import org.komapper.core.dsl.command.SqlInsertCommand
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.scope.ValuesDeclaration
import org.komapper.core.dsl.scope.ValuesScope
import org.komapper.core.metamodel.EntityMetamodel

interface SqlInsertQuery : Query<Pair<Int, Long?>> {
    fun values(declaration: ValuesDeclaration): SqlInsertQuery
}

internal data class SqlInsertQueryImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: SqlInsertContext<ENTITY> = SqlInsertContext(entityMetamodel)
) : SqlInsertQuery {

    override fun values(declaration: ValuesDeclaration): SqlInsertQueryImpl<ENTITY> {
        val scope = ValuesScope()
        declaration(scope)
        val newContext = context.addValues(scope.context.toList())
        return SqlInsertQueryImpl(entityMetamodel, newContext)
    }

    override fun run(config: DatabaseConfig): Pair<Int, Long?> {
        val statement = buildStatement(config.dialect, context)
        val command = SqlInsertCommand(entityMetamodel, config, statement)
        return command.execute()
    }

    override fun peek(dialect: Dialect): Statement {
        return buildStatement(dialect, context)
    }

    private fun buildStatement(dialect: Dialect, c: SqlInsertContext<ENTITY>): Statement {
        val builder = SqlInsertStatementBuilder(dialect, c)
        return builder.build()
    }
}
