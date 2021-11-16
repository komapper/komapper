package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.declaration.SetDeclaration
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlUpdateOptions
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.visitor.QueryVisitor

interface SqlUpdateQuery<ENTITY : Any> : Query<Int> {
    fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQuery<ENTITY>
    fun where(declaration: WhereDeclaration): SqlUpdateQuery<ENTITY>
    fun options(configure: (SqlUpdateOptions) -> SqlUpdateOptions): SqlUpdateQuery<ENTITY>
}

internal data class SqlUpdateQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlUpdateContext<ENTITY, ID, META>,
    private val options: SqlUpdateOptions = SqlUpdateOptions.default
) : SqlUpdateQuery<ENTITY> {

    override fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQuery<ENTITY> {
        val newContext = context.copy(set = context.set + declaration)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlUpdateQuery<ENTITY> {
        val newContext = context.copy(where = context.where + declaration)
        return copy(context = newContext)
    }

    override fun options(configure: (SqlUpdateOptions) -> SqlUpdateOptions): SqlUpdateQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlUpdateQuery(context, options)
    }
}
