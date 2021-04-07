package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.expr.PropertyExpression
import org.komapper.core.dsl.scope.EntitySelectOptionDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration

interface EntityFindQuery<ENTITY : Any, R> : Query<R> {
    fun where(declaration: WhereDeclaration): EntityFindQuery<ENTITY, R>
    fun orderBy(vararg items: PropertyExpression<*>): EntityFindQuery<ENTITY, R>
    fun offset(value: Int): EntityFindQuery<ENTITY, R>
    fun limit(value: Int): EntityFindQuery<ENTITY, R>
    fun forUpdate(): EntityFindQuery<ENTITY, R>
    fun option(declaration: EntitySelectOptionDeclaration): EntityFindQuery<ENTITY, R>
}

internal data class EntityFindQueryImpl<ENTITY : Any, R>(
    private val query: EntitySelectQuery<ENTITY>,
    private val transformer: (ListQuery<ENTITY>) -> Query<R>
) :
    EntityFindQuery<ENTITY, R> {

    override fun where(declaration: WhereDeclaration): EntityFindQueryImpl<ENTITY, R> {
        val newQuery = query.where(declaration)
        return copy(query = newQuery)
    }

    override fun orderBy(vararg items: PropertyExpression<*>): EntityFindQueryImpl<ENTITY, R> {
        val newQuery = query.orderBy(*items)
        return copy(query = newQuery)
    }

    override fun limit(value: Int): EntityFindQuery<ENTITY, R> {
        val newQuery = query.limit(value)
        return copy(query = newQuery)
    }

    override fun offset(value: Int): EntityFindQueryImpl<ENTITY, R> {
        val newQuery = query.offset(value)
        return copy(query = newQuery)
    }

    override fun forUpdate(): EntityFindQueryImpl<ENTITY, R> {
        val newQuery = query.forUpdate()
        return copy(query = newQuery)
    }

    override fun option(declaration: EntitySelectOptionDeclaration): EntityFindQuery<ENTITY, R> {
        val newQuery = query.option(declaration)
        return copy(query = newQuery)
    }

    override fun run(config: DatabaseConfig): R {
        return transformer(query).run(config)
    }

    override fun dryRun(dialect: Dialect): Statement {
        return query.dryRun(dialect)
    }
}
