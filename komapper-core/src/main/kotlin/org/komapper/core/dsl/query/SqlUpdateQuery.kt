package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlUpdateOptions
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
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

    override fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQueryImpl<ENTITY, ID, META> {
        val scope = SetScope<ENTITY>().apply(declaration)
        val newContext = context.copy(set = context.set + scope)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlUpdateQueryImpl<ENTITY, ID, META> {
        val scope = WhereScope().apply(declaration)
        val newContext = context.copy(where = context.where + scope)
        return copy(context = newContext)
    }

    override fun options(configure: (SqlUpdateOptions) -> SqlUpdateOptions): SqlUpdateQueryImpl<ENTITY, ID, META> {
        return copy(options = configure(options))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlUpdateQuery(context, options)
    }
}
