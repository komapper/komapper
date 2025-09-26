package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to delete entities.
 * This query returns Unit.
 */
interface EntityDeleteQuery : Query<Unit> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteQuery
}

/**
 * Represents a query to delete a single entity.
 *
 * @param ENTITY the entity type
 */
interface EntityDeleteSingleQuery<ENTITY : Any> : EntityDeleteQuery {
    /**
     * Indicates to retrieve an entity.
     *
     * @return the query
     */
    fun returning(): EntityDeleteReturningQuery<ENTITY?>

    /**
     * Indicates to retrieve a property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityDeleteReturningQuery<A?>

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
    ): EntityDeleteReturningQuery<Pair<A?, B?>?>

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
    ): EntityDeleteReturningQuery<Triple<A?, B?, C?>?>

    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteSingleQuery<ENTITY>
}

internal data class EntityDeleteSingleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityDeleteSingleQuery<ENTITY> {
    override fun returning(): EntityDeleteReturningQuery<ENTITY?> {
        val newContext = context.copy(returning = Returning.Metamodel(context.target))
        return EntityDeleteSingleReturningQuery(newContext, entity)
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityDeleteReturningQuery<A?> {
        val newContext = context.copy(returning = Returning.Expressions(listOf(expression)))
        return EntityDeleteSingleReturningSingleColumnQuery(newContext, entity, expression)
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): EntityDeleteReturningQuery<Pair<A?, B?>?> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return EntityDeleteSingleReturningPairColumnsQuery(newContext, entity, expressions)
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): EntityDeleteReturningQuery<Triple<A?, B?, C?>?> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return EntityDeleteSingleReturningTripleColumnsQuery(newContext, entity, expressions)
    }

    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteSingleQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteSingleQuery(context, entity)
    }
}

internal data class EntityDeleteBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityDeleteQuery {
    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteQuery {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteBatchQuery(context, entities)
    }
}
