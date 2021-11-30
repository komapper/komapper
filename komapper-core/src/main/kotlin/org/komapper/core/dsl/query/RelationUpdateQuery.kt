package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface RelationUpdateQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : Query<Int> {
    fun set(declaration: AssignmentDeclaration<ENTITY, META>): RelationUpdateQuery<ENTITY, ID, META>
    fun where(declaration: WhereDeclaration): RelationUpdateQuery<ENTITY, ID, META>
    fun options(configure: (UpdateOptions) -> UpdateOptions): RelationUpdateQuery<ENTITY, ID, META>
}

internal data class RelationUpdateQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
) : RelationUpdateQuery<ENTITY, ID, META> {

    override fun set(declaration: AssignmentDeclaration<ENTITY, META>): RelationUpdateQuery<ENTITY, ID, META> {
        val newContext = context.copy(set = context.set + declaration)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): RelationUpdateQuery<ENTITY, ID, META> {
        val newContext = context.copy(where = context.where + declaration)
        return copy(context = newContext)
    }

    override fun options(configure: (UpdateOptions) -> UpdateOptions): RelationUpdateQuery<ENTITY, ID, META> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationUpdateQuery(context)
    }
}
