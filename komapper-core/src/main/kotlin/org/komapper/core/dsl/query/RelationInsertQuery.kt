package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
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

interface RelationInsertValuesQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : RelationInsertQuery<ENTITY, ID, META, Pair<Long, ID?>> {

    /**
     * Indicates to retrieve an entity.
     *
     * @return the query
     */
    fun returning(): RelationInsertReturningQuery<ENTITY>

    /**
     * Indicates to retrieve a property.
     *
     * @param expression the property
     * @return the query
     */
    fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): RelationInsertReturningQuery<A?>

    /**
     * Indicates to retrieve a property pair.
     *
     * @param expression1 the first property
     * @param expression2 the second property
     * @return the query
     */
    fun <A : Any, B : Any> returning(expression1: PropertyMetamodel<ENTITY, A, *>, expression2: PropertyMetamodel<ENTITY, B, *>): RelationInsertReturningQuery<Pair<A?, B?>>

    /**
     * Indicates to retrieve a property triple.
     *
     * @param expression1 the first property
     * @param expression2 the second property
     * @param expression3 the third property
     * @return the query
     */
    fun <A : Any, B : Any, C : Any> returning(expression1: PropertyMetamodel<ENTITY, A, *>, expression2: PropertyMetamodel<ENTITY, B, *>, expression3: PropertyMetamodel<ENTITY, C, *>): RelationInsertReturningQuery<Triple<A?, B?, C?>>

    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertValuesQuery<ENTITY, ID, META>
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
        return RelationInsertValuesQueryImpl(newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertSelectQuery(context)
    }
}

internal data class RelationInsertValuesQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertValuesQuery<ENTITY, ID, META> {

    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertValuesQuery<ENTITY, ID, META> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertValuesQuery<ENTITY, ID, META> {
        val newContext = context.copy(values = context.values + declaration)
        return copy(context = newContext)
    }

    override fun returning(): RelationInsertReturningQuery<ENTITY> {
        val newContext = context.copy(returning = Returning.Metamodel(context.target))
        return RelationInsertValuesReturningQuery(newContext)
    }

    override fun <A : Any> returning(expression: PropertyMetamodel<ENTITY, A, *>): RelationInsertReturningQuery<A?> {
        val newContext = context.copy(returning = Returning.Expressions(listOf(expression)))
        return RelationInsertValuesReturningSingleColumnQuery(newContext, expression)
    }

    override fun <A : Any, B : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
    ): RelationInsertReturningQuery<Pair<A?, B?>> {
        val expressions = expression1 to expression2
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return RelationInsertValuesReturningPairColumnsQuery(newContext, expressions)
    }

    override fun <A : Any, B : Any, C : Any> returning(
        expression1: PropertyMetamodel<ENTITY, A, *>,
        expression2: PropertyMetamodel<ENTITY, B, *>,
        expression3: PropertyMetamodel<ENTITY, C, *>,
    ): RelationInsertReturningQuery<Triple<A?, B?, C?>> {
        val expressions = Triple(expression1, expression2, expression3)
        val newContext = context.copy(returning = Returning.Expressions(expressions.toList()))
        return RelationInsertValuesReturningTripleColumnsQuery(newContext, expressions)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertValuesQuery(context)
    }
}
