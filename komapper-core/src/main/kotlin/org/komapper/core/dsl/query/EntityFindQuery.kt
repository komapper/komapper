package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.metamodel.ColumnInfo

interface EntityFindQuery<ENTITY> : Query<ENTITY> {
    fun where(declaration: WhereDeclaration): EntityFindQuery<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): EntityFindQuery<ENTITY>
    fun offset(value: Int): EntityFindQuery<ENTITY>
    fun forUpdate(): EntityFindQuery<ENTITY>
}

internal data class EntityFirstQuery<ENTITY>(
    private val query: EntitySelectQuery<ENTITY>
) :
    EntityFindQuery<ENTITY> {

    override fun where(declaration: WhereDeclaration): EntityFirstQuery<ENTITY> {
        val newQuery = query.where(declaration)
        return copy(query = newQuery)
    }

    override fun orderBy(vararg items: ColumnInfo<*>): EntityFirstQuery<ENTITY> {
        val newQuery = query.orderBy(*items)
        return copy(query = newQuery)
    }

    override fun offset(value: Int): EntityFirstQuery<ENTITY> {
        val newQuery = query.offset(value)
        return copy(query = newQuery)
    }

    override fun forUpdate(): EntityFirstQuery<ENTITY> {
        val newQuery = query.forUpdate()
        return copy(query = newQuery)
    }

    override fun execute(config: DatabaseConfig): ENTITY {
        return query.first().execute(config)
    }

    override fun statement(dialect: Dialect): Statement {
        return query.statement(dialect)
    }
}

internal data class EntityFirstOrNullQuery<ENTITY>(
    private val query: EntitySelectQuery<ENTITY>
) :
    EntityFindQuery<ENTITY?> {

    override fun where(declaration: WhereDeclaration): EntityFirstOrNullQuery<ENTITY> {
        val newQuery = query.where(declaration)
        return copy(query = newQuery)
    }

    override fun orderBy(vararg items: ColumnInfo<*>): EntityFirstOrNullQuery<ENTITY> {
        val newQuery = query.orderBy(*items)
        return copy(query = newQuery)
    }

    override fun offset(value: Int): EntityFirstOrNullQuery<ENTITY> {
        val newQuery = query.offset(value)
        return copy(query = newQuery)
    }

    override fun forUpdate(): EntityFirstOrNullQuery<ENTITY> {
        val newQuery = query.forUpdate()
        return copy(query = newQuery)
    }

    override fun execute(config: DatabaseConfig): ENTITY? {
        return query.firstOrNull().execute(config)
    }

    override fun statement(dialect: Dialect): Statement {
        return query.statement(dialect)
    }
}
