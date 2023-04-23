package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
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
interface RelationUpdateQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : Query<Long> {
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

    /**
     * Indicates to retrieve an entity.
     *
     * @return the query
     */
    fun returning(): RelationUpdateReturningQuery<List<ENTITY>>

    /**
     * Indicates to retrieve a property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): RelationUpdateReturningQuery<List<A?>>

    /**
     * Indicates to retrieve a property pair.
     *
     * @param expression1 the first property
     * @param expression2 the second property
     * @return the query
     */
    fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): RelationUpdateReturningQuery<List<Pair<A?, B?>>>

    /**
     * Indicates to retrieve a property triple.
     *
     * @param expression1 the first property
     * @param expression2 the second property
     * @param expression3 the third property
     * @return the query
     */
    fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): RelationUpdateReturningQuery<List<Triple<A?, B?, C?>>>
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

    override fun returning(): RelationUpdateReturningQuery<List<ENTITY>> {
        val newContext = context.copy(returning = Returning.Metamodel(context.target))
        return RelationUpdateReturningQueryImpl(newContext)
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): RelationUpdateReturningQuery<List<A?>> {
        val newContext = context.copy(returning = Returning.Expressions(listOf(expression)))
        return RelationUpdateReturningSingleColumnQuery(newContext, expression)
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): RelationUpdateReturningQuery<List<Pair<A?, B?>>> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return RelationUpdateReturningPairColumnsQuery(newContext, expressions)
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): RelationUpdateReturningQuery<List<Triple<A?, B?, C?>>> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return RelationUpdateReturningTripleColumnsQuery(newContext, expressions)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationUpdateQuery(context)
    }
}
