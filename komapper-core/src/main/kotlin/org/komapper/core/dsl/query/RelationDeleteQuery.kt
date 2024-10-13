package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to delete rows.
 * This query returns the number of rows affected.
 *
 * @param ENTITY the entity type
 */
interface RelationDeleteQuery<ENTITY : Any> : Query<Long> {
    /**
     * Builds a query with a WHERE clause.
     *
     * @param declaration the where declaration
     * @return the query
     */
    fun where(declaration: WhereDeclaration): RelationDeleteQuery<ENTITY>

    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (DeleteOptions) -> DeleteOptions): RelationDeleteQuery<ENTITY>

    /**
     * Indicates to retrieve an entity.
     *
     * @return the query
     */
    fun returning(): RelationDeleteReturningQuery<List<ENTITY>>

    /**
     * Indicates to retrieve a property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): RelationDeleteReturningQuery<List<A?>>

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
    ): RelationDeleteReturningQuery<List<Pair<A?, B?>>>

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
    ): RelationDeleteReturningQuery<List<Triple<A?, B?, C?>>>
}

internal data class RelationDeleteQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
) : RelationDeleteQuery<ENTITY> {
    override fun where(declaration: WhereDeclaration): RelationDeleteQuery<ENTITY> {
        val newContext = context.copy(where = context.where + declaration)
        return copy(context = newContext)
    }

    override fun returning(): RelationDeleteReturningQuery<List<ENTITY>> {
        val newContext = context.copy(returning = Returning.Metamodel(context.target))
        return RelationDeleteReturningQueryImpl(newContext)
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): RelationDeleteReturningQuery<List<A?>> {
        val newContext = context.copy(returning = Returning.Expressions(listOf(expression)))
        return RelationDeleteReturningSingleColumnQuery(newContext, expression)
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): RelationDeleteReturningQuery<List<Pair<A?, B?>>> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return RelationDeleteReturningPairColumnsQuery(newContext, expressions)
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): RelationDeleteReturningQuery<List<Triple<A?, B?, C?>>> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return RelationDeleteReturningTripleColumnsQuery(newContext, expressions)
    }

    override fun options(configure: (DeleteOptions) -> DeleteOptions): RelationDeleteQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationDeleteQuery(context)
    }
}
