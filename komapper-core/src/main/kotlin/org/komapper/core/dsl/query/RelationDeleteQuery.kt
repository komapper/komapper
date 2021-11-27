package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface RelationDeleteQuery : Query<Int> {
    fun where(declaration: WhereDeclaration): RelationDeleteQuery
    fun options(configure: (DeleteOptions) -> DeleteOptions): RelationDeleteQuery
}

internal data class RelationDeleteQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
) : RelationDeleteQuery {

    override fun where(declaration: WhereDeclaration): RelationDeleteQuery {
        val newContext = context.copy(where = context.where + declaration)
        return copy(context = newContext)
    }

    override fun options(configure: (DeleteOptions) -> DeleteOptions): RelationDeleteQuery {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationDeleteQuery(context)
    }
}
