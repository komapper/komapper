package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to update rows.
 * This query returns the number of rows affected.
 *
 * @param ENTITY the entity type
 * @param ID the entity id type
 * @param META the entity metamodel type
 */
interface RelationUpdateQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : Query<Int> {
    /**
     * Builds a query with a SET clause.
     *
     * @param declaration the assignment declaration
     * @return the query
     */
    fun set(declaration: AssignmentDeclaration<ENTITY, META>): RelationUpdateQuery<ENTITY, ID, META>

    /**
     * Builds a query with a WHERE clause.
     *
     * @param declaration the where declaration
     * @return the query
     */
    fun where(declaration: WhereDeclaration): RelationUpdateQuery<ENTITY, ID, META>

    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
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
