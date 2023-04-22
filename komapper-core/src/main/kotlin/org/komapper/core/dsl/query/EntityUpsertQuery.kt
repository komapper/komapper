package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.element.Output
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to upsert entities.
 * This query returns a numeric value or a list of numeric values specific to the driver.
 *
 * @param T the result type
 */
interface EntityUpsertQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<T>
}

/**
 * Represents a query to upsert a single entity.
 * The [returning] functions fetch the result with the [single] function.
 *
 * @param ENTITY the entity type
 */
interface EntityUpsertSingleQueryNonNull<ENTITY : Any> : EntityUpsertQuery<Long> {
    /**
     * Indicates to retrieve an entity.
     * @return the query
     */
    fun returning(): EntityUpsertReturningQuery<ENTITY>

    /**
     * Indicates to retrieve a property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityUpsertReturningQuery<A?>

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
    ): EntityUpsertReturningQuery<Pair<A?, B?>>

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
    ): EntityUpsertReturningQuery<Triple<A?, B?, C?>>

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertSingleQueryNonNull<ENTITY>
}

/**
 * Represents a query to upsert a single entity.
 * The [returning] functions fetch the result with the [singleOrNull] function.
 *
 * @param ENTITY the entity type
 */
interface EntityUpsertSingleQueryNullable<ENTITY : Any> : EntityUpsertQuery<Long> {
    /**
     * Indicates to retrieve an entity.
     * @return the query
     */
    fun returning(): EntityUpsertReturningQuery<ENTITY?>

    /**
     * Indicates to retrieve a property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityUpsertReturningQuery<A?>

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
    ): EntityUpsertReturningQuery<Pair<A?, B?>?>

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
    ): EntityUpsertReturningQuery<Triple<A?, B?, C?>?>

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertSingleQueryNullable<ENTITY>
}

/**
 * Represents a query to upsert multiple entities.
 *
 * @param ENTITY the entity type
 */
interface EntityUpsertMultipleQuery<ENTITY : Any> : EntityUpsertQuery<Long> {
    /**
     * Indicates to retrieve an entity.
     * @return the query
     */
    fun returning(): EntityUpsertReturningQuery<List<ENTITY>>

    /**
     * Indicates to retrieve a property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityUpsertReturningQuery<List<A?>>

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
    ): EntityUpsertReturningQuery<List<Pair<A?, B?>>>

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
    ): EntityUpsertReturningQuery<List<Triple<A?, B?, C?>>>

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertMultipleQuery<ENTITY>
}

internal data class EntityUpsertSingleQueryNonNullImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpsertSingleQueryNonNull<ENTITY> {
    override fun returning(): EntityUpsertReturningQuery<ENTITY> {
        val newContext = context.copy(returning = Output.Metamodel(context.target))
        return EntityUpsertSingleReturningQuery(newContext, entity) { it.single() }
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityUpsertReturningQuery<A?> {
        val newContext = context.copy(returning = Output.Expressions(listOf(expression)))
        return EntityUpsertSingleReturningSingleColumnQuery(newContext, entity, expression) { it.single() }
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): EntityUpsertReturningQuery<Pair<A?, B?>> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Output.Expressions(expressions.toList()))
        return EntityUpsertSingleReturningPairColumnsQuery(newContext, entity, expressions) { it.single() }
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): EntityUpsertReturningQuery<Triple<A?, B?, C?>> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Output.Expressions(expressions.toList()))
        return EntityUpsertSingleReturningTripleColumnsQuery(newContext, entity, expressions) { it.single() }
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertSingleQueryNonNull<ENTITY> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleQuery(context, entity)
    }
}

internal data class EntityUpsertSingleQueryNullableImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpsertSingleQueryNullable<ENTITY> {
    override fun returning(): EntityUpsertReturningQuery<ENTITY?> {
        val newContext = context.copy(returning = Output.Metamodel(context.target))
        return EntityUpsertSingleReturningQuery(newContext, entity) { it.singleOrNull() }
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityUpsertReturningQuery<A?> {
        val newContext = context.copy(returning = Output.Expressions(listOf(expression)))
        return EntityUpsertSingleReturningSingleColumnQuery(newContext, entity, expression) { it.singleOrNull() }
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): EntityUpsertReturningQuery<Pair<A?, B?>?> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Output.Expressions(expressions.toList()))
        return EntityUpsertSingleReturningPairColumnsQuery(newContext, entity, expressions) { it.singleOrNull() }
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): EntityUpsertReturningQuery<Triple<A?, B?, C?>?> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Output.Expressions(expressions.toList()))
        return EntityUpsertSingleReturningTripleColumnsQuery(newContext, entity, expressions) { it.single() }
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertSingleQueryNullable<ENTITY> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleQuery(context, entity)
    }
}

internal data class EntityUpsertSingleUpdateQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : Query<ENTITY> {

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleUpdateQuery(context, entity)
    }
}

internal data class EntityUpsertSingleIgnoreQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpsertQuery<ENTITY?> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<ENTITY?> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleIgnoreQuery(context, entity)
    }
}

internal data class EntityUpsertMultipleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertMultipleQuery<ENTITY> {
    override fun returning(): EntityUpsertReturningQuery<List<ENTITY>> {
        val newContext = context.copy(returning = Output.Metamodel(context.target))
        return EntityUpsertMultipleReturningQuery(newContext, entities)
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): EntityUpsertReturningQuery<List<A?>> {
        val newContext = context.copy(returning = Output.Expressions(listOf(expression)))
        return EntityUpsertMultipleReturningSingleColumnQuery(newContext, entities, expression)
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): EntityUpsertReturningQuery<List<Pair<A?, B?>>> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Output.Expressions(expressions.toList()))
        return EntityUpsertMultipleReturningPairColumnsQuery(newContext, entities, expressions)
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): EntityUpsertReturningQuery<List<Triple<A?, B?, C?>>> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Output.Expressions(expressions.toList()))
        return EntityUpsertMultipleReturningTripleColumnsQuery(newContext, entities, expressions)
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertMultipleQuery<ENTITY> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertMultipleQuery(context, entities)
    }
}

data class EntityUpsertBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertQuery<List<Long>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<List<Long>> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertBatchQuery(context, entities)
    }
}
