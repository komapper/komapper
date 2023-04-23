package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to update entities.
 * This query returns new entity or entities.
 *
 * @param T the result type
 */
interface EntityUpdateQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateQuery<T>
}

/**
 * Represents a query to update a single entity.
 *
 * @param ENTITY the entity type
 */
interface EntityUpdateSingleQuery<ENTITY : Any> : EntityUpdateQuery<ENTITY> {
    /**
     * Indicates to retrieve an entity.
     * @return the query
     */
    fun returning(): EntityUpdateReturningQuery<ENTITY?>

    /**
     * Indicates to retrieve a property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityUpdateReturningQuery<A?>

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
    ): EntityUpdateReturningQuery<Pair<A?, B?>?>

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
    ): EntityUpdateReturningQuery<Triple<A?, B?, C?>?>

    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateSingleQuery<ENTITY>
}

internal data class EntityUpdateSingleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpdateSingleQuery<ENTITY> {
    override fun returning(): EntityUpdateReturningQuery<ENTITY?> {
        val newContext = context.copy(returning = Returning.Metamodel(context.target))
        return EntityUpdateSingleReturningQuery(newContext, entity)
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityUpdateReturningQuery<A?> {
        val newContext = context.copy(returning = Returning.Expressions(listOf(expression)))
        return EntityUpdateSingleReturningSingleColumnQuery(newContext, entity, expression)
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): EntityUpdateReturningQuery<Pair<A?, B?>?> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return EntityUpdateSingleReturningPairColumnsQuery(newContext, entity, expressions)
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): EntityUpdateReturningQuery<Triple<A?, B?, C?>?> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return EntityUpdateSingleReturningTripleColumnsQuery(newContext, entity, expressions)
    }

    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateSingleQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateSingleQuery(context, entity)
    }
}

internal data class EntityUpdateBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpdateQuery<List<ENTITY>> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateQuery<List<ENTITY>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateBatchQuery(context, entities)
    }
}
