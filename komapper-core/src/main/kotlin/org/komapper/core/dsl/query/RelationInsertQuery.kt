package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents the query to insert rows.
 * This query returns a pair containing the number of rows affected and the ID(s) generated.
 *
 * @param ENTITY the entity type
 * @param ID the entity id type
 * @param META the entity metamodel type
 * @param R the result type of query
 */
interface RelationInsertQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> : Query<R> {
    /**
     * Builds a query with a VALUES clause.
     *
     * @param declaration the assignment declaration
     * @return the query
     */
    fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertQuery<ENTITY, ID, META, Pair<Long, ID?>>

    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertQuery<ENTITY, ID, META, R>
}

internal data class RelationInsertSelectQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertSelectContext<ENTITY, ID, META>,
) : RelationInsertQuery<ENTITY, ID, META, Pair<Long, List<ID>>> {

    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertQuery<ENTITY, ID, META, Pair<Long, List<ID>>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertQuery<ENTITY, ID, META, Pair<Long, ID?>> {
        val newContext = context.asRelationInsertValuesContext(declaration)
        return RelationInsertValuesQuery(newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertSelectQuery(context)
    }
}

internal data class RelationInsertValuesQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertQuery<ENTITY, ID, META, Pair<Long, ID?>> {

    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertQuery<ENTITY, ID, META, Pair<Long, ID?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertQuery<ENTITY, ID, META, Pair<Long, ID?>> {
        val newContext = context.copy(values = context.values + declaration)
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertValuesQuery(context)
    }
}
