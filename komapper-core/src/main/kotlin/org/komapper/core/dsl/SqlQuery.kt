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
import org.komapper.core.dsl.query.SqlInsertQuery
import org.komapper.core.dsl.query.SqlInsertQueryImpl
import org.komapper.core.dsl.query.SqlSelectQuery
import org.komapper.core.dsl.query.SqlSelectQueryImpl
import org.komapper.core.dsl.query.SqlUpdateQuery
import org.komapper.core.dsl.query.SqlUpdateQueryImpl
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.ValuesDeclaration

object SqlQuery : Dsl {

    fun <ENTITY> first(entityMetamodel: EntityMetamodel<ENTITY>): SqlFindQuery<ENTITY, ENTITY> {
        return createFindQuery(entityMetamodel) { it.first() }
    }

    fun <ENTITY> firstOrNull(entityMetamodel: EntityMetamodel<ENTITY>): SqlFindQuery<ENTITY, ENTITY?> {
        return createFindQuery(entityMetamodel) { it.firstOrNull() }
    }

    private fun <ENTITY, R> createFindQuery(
        entityMetamodel: EntityMetamodel<ENTITY>,
        transformer: (ListQuery<ENTITY>) -> Query<R>
    ): SqlFindQuery<ENTITY, R> {
        val selectQuery = SqlSelectQueryImpl(SqlSelectContext(entityMetamodel)).limit(1)
        return SqlFindQueryImpl(selectQuery, transformer)
    }

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(SqlSelectContext(entityMetamodel))
    }

    fun <ENTITY> insert(entityMetamodel: EntityMetamodel<ENTITY>, declaration: ValuesDeclaration): SqlInsertQuery {
        return SqlInsertQueryImpl(SqlInsertContext(entityMetamodel)).values(declaration)
    }

    fun <ENTITY> update(entityMetamodel: EntityMetamodel<ENTITY>, declaration: SetDeclaration): SqlUpdateQuery {
        return SqlUpdateQueryImpl(SqlUpdateContext(entityMetamodel)).set(declaration)
    }

    fun <ENTITY> delete(entityMetamodel: EntityMetamodel<ENTITY>): SqlDeleteQuery {
        return SqlDeleteQueryImpl(SqlDeleteContext(entityMetamodel))
    }
}
