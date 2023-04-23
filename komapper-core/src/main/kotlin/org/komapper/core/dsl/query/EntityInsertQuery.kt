package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to insert entities.
 * This query returns new entity or entities.
 *
 * @param T the result type
 */
interface EntityInsertQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<T>
}

/**
 * Represents a query to insert a single entity.
 *
 * @param ENTITY the entity type
 */
interface EntityInsertSingleQuery<ENTITY : Any> : EntityInsertQuery<ENTITY> {
    /**
     * Indicates to retrieve an entity.
     *
     * @return the query
     */
    fun returning(): EntityInsertReturningQuery<ENTITY>

    /**
     * Indicates to retrieve a property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityInsertReturningQuery<A?>

    /**
     * Indicates to retrieve a property pair.
     *
     * @param expression1 the first property
     * @param expression2 the second property
     * @return the query
     */
    fun <A : Any, B : Any> returning(expression1: PropertyMetamodel<ENTITY, A, *>, expression2: PropertyMetamodel<ENTITY, B, *>): EntityInsertReturningQuery<Pair<A?, B?>>

    /**
     * Indicates to retrieve a property triple.
     *
     * @param expression1 the first property
     * @param expression2 the second property
     * @param expression3 the third property
     * @return the query
     */
    fun <A : Any, B : Any, C : Any> returning(expression1: PropertyMetamodel<ENTITY, A, *>, expression2: PropertyMetamodel<ENTITY, B, *>, expression3: PropertyMetamodel<ENTITY, C, *>): EntityInsertReturningQuery<Triple<A?, B?, C?>>

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertSingleQuery<ENTITY>
}

/**
 * Represents a query to insert multiple entities.
 *
 * @param ENTITY the entity type
 */
interface EntityInsertMultipleQuery<ENTITY : Any> : EntityInsertQuery<List<ENTITY>> {
    /**
     * Indicates to retrieve a list of entity.
     *
     * @return the query
     */
    fun returning(): EntityInsertReturningQuery<List<ENTITY>>

    /**
     * Indicates to retrieve a list of property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityInsertReturningQuery<List<A?>>

    /**
     * Indicates to retrieve a list of property pair.
     *
     * @param expression1 the first property
     * @param expression2 the second property
     * @return the query
     */
    fun <A : Any, B : Any> returning(expression1: PropertyMetamodel<ENTITY, A, *>, expression2: PropertyMetamodel<ENTITY, B, *>): EntityInsertReturningQuery<List<Pair<A?, B?>>>

    /**
     * Indicates to retrieve a list of property triple.
     *
     * @param expression1 the first property
     * @param expression2 the second property
     * @param expression3 the third property
     * @return the query
     */
    fun <A : Any, B : Any, C : Any> returning(expression1: PropertyMetamodel<ENTITY, A, *>, expression2: PropertyMetamodel<ENTITY, B, *>, expression3: PropertyMetamodel<ENTITY, C, *>): EntityInsertReturningQuery<List<Triple<A?, B?, C?>>>

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertMultipleQuery<ENTITY>
}

internal data class EntityInsertSingleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityInsertSingleQuery<ENTITY> {
    override fun returning(): EntityInsertReturningQuery<ENTITY> {
        val newContext = context.copy(returning = Returning.Metamodel(context.target))
        return EntityInsertSingleReturningQuery(newContext, entity)
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityInsertReturningQuery<A?> {
        val newContext = context.copy(returning = Returning.Expressions(listOf(expression)))
        return EntityInsertSingleReturningSingleColumnQuery(newContext, entity, expression)
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): EntityInsertReturningQuery<Pair<A?, B?>> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return EntityInsertSingleReturningPairColumnsQuery(newContext, entity, expressions)
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): EntityInsertReturningQuery<Triple<A?, B?, C?>> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return EntityInsertSingleReturningTripleColumnsQuery(newContext, entity, expressions)
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertSingleQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertSingleQuery(context, entity)
    }
}

internal data class EntityInsertMultipleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityInsertMultipleQuery<ENTITY> {
    override fun returning(): EntityInsertReturningQuery<List<ENTITY>> {
        val newContext = context.copy(returning = Returning.Metamodel(context.target))
        return EntityInsertMultipleReturningQuery(newContext, entities)
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityInsertReturningQuery<List<A?>> {
        val newContext = context.copy(returning = Returning.Expressions(listOf(expression)))
        return EntityInsertMultipleReturningSingleColumnQuery(newContext, entities, expression)
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): EntityInsertReturningQuery<List<Pair<A?, B?>>> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return EntityInsertMultipleReturningPairColumnsQuery(newContext, entities, expressions)
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): EntityInsertReturningQuery<List<Triple<A?, B?, C?>>> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return EntityInsertMultipleReturningTripleColumnsQuery(newContext, entities, expressions)
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertMultipleQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertMultipleQuery(context, entities)
    }
}

internal data class EntityInsertBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityInsertQuery<List<ENTITY>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<List<ENTITY>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertBatchQuery(context, entities)
    }
}
