package org.komapper.core.dsl

import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.SqlDeleteQuery
import org.komapper.core.dsl.query.SqlDeleteQueryImpl
import org.komapper.core.dsl.query.SqlFindQuery
import org.komapper.core.dsl.query.SqlFindQueryImpl
import org.komapper.core.dsl.query.SqlInsertQueryBuilder
import org.komapper.core.dsl.query.SqlInsertQueryBuilderImpl
import org.komapper.core.dsl.query.SqlInsertQueryImpl
import org.komapper.core.dsl.query.SqlSelectQuery
import org.komapper.core.dsl.query.SqlSelectQueryImpl
import org.komapper.core.dsl.query.SqlUpdateQuery
import org.komapper.core.dsl.query.SqlUpdateQueryImpl
import org.komapper.core.dsl.scope.SetDeclaration

object SqlQuery : Dsl {

    fun <ENTITY : Any> first(entityMetamodel: EntityMetamodel<ENTITY>): SqlFindQuery<ENTITY, ENTITY> {
        return createFindQuery(entityMetamodel) { it.first() }
    }

    fun <ENTITY : Any> firstOrNull(entityMetamodel: EntityMetamodel<ENTITY>): SqlFindQuery<ENTITY, ENTITY?> {
        return createFindQuery(entityMetamodel) { it.firstOrNull() }
    }

    private fun <ENTITY : Any, R> createFindQuery(
        entityMetamodel: EntityMetamodel<ENTITY>,
        transformer: (ListQuery<ENTITY>) -> Query<R>
    ): SqlFindQuery<ENTITY, R> {
        val selectQuery = SqlSelectQueryImpl(SqlSelectContext(entityMetamodel)).limit(1)
        return SqlFindQueryImpl(selectQuery, transformer)
    }

    fun <ENTITY : Any> from(entityMetamodel: EntityMetamodel<ENTITY>): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(SqlSelectContext(entityMetamodel))
    }

    fun <ENTITY : Any> insert(entityMetamodel: EntityMetamodel<ENTITY>): SqlInsertQueryBuilder<ENTITY> {
        val query = SqlInsertQueryImpl(SqlInsertContext(entityMetamodel))
        return SqlInsertQueryBuilderImpl(query)
    }

    fun <ENTITY : Any> update(
        entityMetamodel: EntityMetamodel<ENTITY>,
        declaration: SetDeclaration<ENTITY>
    ): SqlUpdateQuery<ENTITY> {
        return SqlUpdateQueryImpl(SqlUpdateContext(entityMetamodel)).set(declaration)
    }

    fun <ENTITY : Any> delete(entityMetamodel: EntityMetamodel<ENTITY>): SqlDeleteQuery {
        return SqlDeleteQueryImpl(SqlDeleteContext(entityMetamodel))
    }
}
