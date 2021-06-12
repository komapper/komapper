package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlDeleteOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
import org.komapper.core.dsl.visitor.QueryVisitor

interface SqlDeleteQuery : Query<Int> {
    fun where(declaration: WhereDeclaration): SqlDeleteQuery
    fun option(configure: (SqlDeleteOption) -> SqlDeleteOption): SqlDeleteQuery
}

internal data class SqlDeleteQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlDeleteContext<ENTITY, ID, META>,
    private val option: SqlDeleteOption = SqlDeleteOption.default
) : SqlDeleteQuery {

    override fun where(declaration: WhereDeclaration): SqlDeleteQueryImpl<ENTITY, ID, META> {
        val scope = WhereScope().apply(declaration)
        val newContext = context.copy(where = context.where + scope)
        return copy(context = newContext)
    }

    override fun option(configure: (SqlDeleteOption) -> SqlDeleteOption): SqlDeleteQueryImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlDeleteQuery(context, option)
    }
}
