package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlDeleteOptions
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
import org.komapper.core.dsl.visitor.QueryVisitor

interface SqlDeleteQuery : Query<Int> {
    fun where(declaration: WhereDeclaration): SqlDeleteQuery
    fun options(configure: (SqlDeleteOptions) -> SqlDeleteOptions): SqlDeleteQuery
}

internal data class SqlDeleteQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlDeleteContext<ENTITY, ID, META>,
    private val options: SqlDeleteOptions = SqlDeleteOptions.default
) : SqlDeleteQuery {

    override fun where(declaration: WhereDeclaration): SqlDeleteQuery {
        val scope = WhereScope().apply(declaration)
        val newContext = context.copy(where = context.where + scope)
        return copy(context = newContext)
    }

    override fun options(configure: (SqlDeleteOptions) -> SqlDeleteOptions): SqlDeleteQuery {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlDeleteQuery(context, options)
    }
}
