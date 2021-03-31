package org.komapper.core.dsl

import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.query.SqlDeleteQuery
import org.komapper.core.dsl.query.SqlDeleteQueryImpl
import org.komapper.core.dsl.query.SqlInsertQuery
import org.komapper.core.dsl.query.SqlInsertQueryImpl
import org.komapper.core.dsl.query.SqlSelectQuery
import org.komapper.core.dsl.query.SqlSelectQueryImpl
import org.komapper.core.dsl.query.SqlUpdateQuery
import org.komapper.core.dsl.query.SqlUpdateQueryImpl
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.ValuesDeclaration
import org.komapper.core.metamodel.EntityMetamodel

object SqlQuery {

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
