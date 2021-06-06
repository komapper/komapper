package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlUpdateOption
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope

interface SqlUpdateQuery<ENTITY : Any> : Query<Int> {
    fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQuery<ENTITY>
    fun where(declaration: WhereDeclaration): SqlUpdateQuery<ENTITY>
    fun option(configure: (SqlUpdateOption) -> SqlUpdateOption): SqlUpdateQuery<ENTITY>
}

data class SqlUpdateQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: SqlUpdateContext<ENTITY, ID, META>,
    val option: SqlUpdateOption = SqlUpdateOption.default
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

    override fun option(configure: (SqlUpdateOption) -> SqlUpdateOption): SqlUpdateQueryImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
