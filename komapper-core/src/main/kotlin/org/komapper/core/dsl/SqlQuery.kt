package org.komapper.core.dsl

import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.SqlDeleteQuery
import org.komapper.core.dsl.query.SqlDeleteQueryImpl
import org.komapper.core.dsl.query.SqlInsertQueryBuilder
import org.komapper.core.dsl.query.SqlInsertQueryBuilderImpl
import org.komapper.core.dsl.query.SqlInsertQueryImpl
import org.komapper.core.dsl.query.SqlSelectQuery
import org.komapper.core.dsl.query.SqlSelectQueryImpl
import org.komapper.core.dsl.query.SqlUpdateQueryBuilder
import org.komapper.core.dsl.query.SqlUpdateQueryBuilderImpl
import org.komapper.core.dsl.query.SqlUpdateQueryImpl
import org.komapper.core.dsl.scope.WhereDeclaration

object SqlQuery : Dsl {

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> first(
        entityMetamodel: META,
        declaration: WhereDeclaration
    ): Query<ENTITY> {
        return SqlSelectQueryImpl(SqlSelectContext(entityMetamodel))
            .where(declaration)
            .limit(1)
            .first()
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> firstOrNull(
        entityMetamodel: META,
        declaration: WhereDeclaration
    ): Query<ENTITY?> {
        return SqlSelectQueryImpl(SqlSelectContext(entityMetamodel))
            .where(declaration)
            .limit(1)
            .firstOrNull()
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> from(entityMetamodel: META): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(SqlSelectContext(entityMetamodel))
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insert(entityMetamodel: META): SqlInsertQueryBuilder<ENTITY> {
        val query = SqlInsertQueryImpl(SqlInsertContext(entityMetamodel))
        return SqlInsertQueryBuilderImpl(query)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> update(entityMetamodel: META): SqlUpdateQueryBuilder<ENTITY> {
        val query = SqlUpdateQueryImpl(SqlUpdateContext(entityMetamodel))
        return SqlUpdateQueryBuilderImpl(query)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> delete(entityMetamodel: META): SqlDeleteQuery {
        return SqlDeleteQueryImpl(SqlDeleteContext(entityMetamodel))
    }
}
