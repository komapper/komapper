package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.SetDeclaration
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface RelationUpdateQuery<ENTITY : Any> : Query<Int> {
    fun set(declaration: SetDeclaration<ENTITY>): RelationUpdateQuery<ENTITY>
    fun where(declaration: WhereDeclaration): RelationUpdateQuery<ENTITY>
    fun options(configure: (UpdateOptions) -> UpdateOptions): RelationUpdateQuery<ENTITY>
}

internal data class RelationUpdateQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
    private val options: UpdateOptions = UpdateOptions.default
) : RelationUpdateQuery<ENTITY> {

    override fun set(declaration: SetDeclaration<ENTITY>): RelationUpdateQuery<ENTITY> {
        val newContext = context.copy(set = context.set + declaration)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): RelationUpdateQuery<ENTITY> {
        val newContext = context.copy(where = context.where + declaration)
        return copy(context = newContext)
    }

    override fun options(configure: (UpdateOptions) -> UpdateOptions): RelationUpdateQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationUpdateQuery(context, options)
    }
}
