package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.scope.SqlSelectOptionDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration

interface SqlFindQuery<ENTITY : Any, R> : Query<R> {
    fun where(declaration: WhereDeclaration): SqlFindQuery<ENTITY, R>
    fun orderBy(vararg items: PropertyExpression<*>): SqlFindQuery<ENTITY, R>
    fun offset(value: Int): SqlFindQuery<ENTITY, R>
    fun forUpdate(): SqlFindQuery<ENTITY, R>
    fun option(declaration: SqlSelectOptionDeclaration): SqlFindQuery<ENTITY, R>
}

internal data class SqlFindQueryImpl<ENTITY : Any, R>(
    private val query: SqlSelectQuery<ENTITY>,
    private val transformer: (ListQuery<ENTITY>) -> Query<R>
) :
    SqlFindQuery<ENTITY, R> {

    override fun where(declaration: WhereDeclaration): SqlFindQueryImpl<ENTITY, R> {
        val newQuery = query.where(declaration)
        return copy(query = newQuery)
    }

    override fun orderBy(vararg items: PropertyExpression<*>): SqlFindQueryImpl<ENTITY, R> {
        val newQuery = query.orderBy(*items)
        return copy(query = newQuery)
    }

    override fun offset(value: Int): SqlFindQueryImpl<ENTITY, R> {
        val newQuery = query.offset(value)
        return copy(query = newQuery)
    }

    override fun forUpdate(): SqlFindQueryImpl<ENTITY, R> {
        val newQuery = query.forUpdate()
        return copy(query = newQuery)
    }

    override fun option(declaration: SqlSelectOptionDeclaration): SqlFindQueryImpl<ENTITY, R> {
        val newQuery = query.option(declaration)
        return copy(query = newQuery)
    }

    override fun run(config: DatabaseConfig): R {
        return transformer(query).run(config)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return query.dryRun(config)
    }
}
