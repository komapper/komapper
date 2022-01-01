package org.komapper.core.dsl.query

import org.komapper.core.dsl.element.Relationship
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.HavingDeclaration
import org.komapper.core.dsl.expression.OnDeclaration
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions

/**
 * Represents the query to retrieve data.
 * @param ENTITY the entity type
 * @param QUERY the query type
 */
interface SelectQuery<ENTITY : Any, QUERY : SelectQuery<ENTITY, QUERY>> :
    FlowSubquery<ENTITY> {
    /**
     * Specifies a DISTINCT keyword.
     * @return the query that returns a list of entities
     */
    fun distinct(): QUERY

    /**
     * Builds a INNER JOIN clause.
     * @return the query that returns a list of entities
     */
    fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(
        metamodel: META2,
        on: OnDeclaration
    ): QUERY = innerJoin(Relationship(metamodel, on))

    /**
     * Builds a LEFT OUTER JOIN clause.
     * @return the query that returns a list of entities
     */
    fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(
        metamodel: META2,
        on: OnDeclaration
    ): QUERY = leftJoin(Relationship(metamodel, on))

    /**
     * Builds a INNER JOIN clause.
     * @return the query that returns a list of entities
     */
    fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(
        relationship: Relationship<ENTITY2, ID2, META2>,
    ): QUERY

    /**
     * Builds the LEFT OUTER JOIN clause.
     * @return the query that returns a list of entities
     */
    fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(
        relationship: Relationship<ENTITY2, ID2, META2>,
    ): QUERY

    /**
     * Builds a WHERE clause.
     * @return the query that returns a list of entities
     */
    fun where(declaration: WhereDeclaration): QUERY

    /**
     * Builds an ORDER BY clause.
     * @return the query that returns a list of entities
     */
    fun orderBy(vararg expressions: SortExpression): QUERY = orderBy(expressions.toList())

    /**
     * Builds an ORDER BY clause.
     * @return the query that returns a list of entities
     */
    fun orderBy(expressions: List<SortExpression>): QUERY

    /**
     * Builds a OFFSET clause.
     * @param offset the offset value. If it is negative, it is ignored.
     * @return the query that returns a list of entities
     */
    fun offset(offset: Int): QUERY

    /**
     * Builds a LIMIT clause.
     * @param limit the limit value. If it is negative or zero, it is ignored.
     * @return the query that returns a list of entities
     */
    fun limit(limit: Int): QUERY

    /**
     * Builds a FOR UPDATE clause.
     * @return the query that returns a list of entities
     */
    fun forUpdate(): QUERY

    /**
     * Builds a query with the options applied.
     * @param configure the configure function to apply options
     * @return the query that returns a list of entities
     */
    fun options(configure: (SelectOptions) -> SelectOptions): QUERY

    /**
     * Builds a GROUP BY clause.
     * @return the query that returns a list of entities
     */
    fun groupBy(vararg expressions: ColumnExpression<*, *>): RelationSelectQuery<ENTITY> = groupBy(expressions.toList())

    /**
     * Builds a GROUP BY clause.
     * @return the query that returns a list of entities
     */
    fun groupBy(expressions: List<ColumnExpression<*, *>>): RelationSelectQuery<ENTITY>

    /**
     * Builds a HAVING clause.
     * @return the query that returns a list of entities
     */
    fun having(declaration: HavingDeclaration): RelationSelectQuery<ENTITY>

    /**
     * Builds a SELECT clause.
     * @param expression the scalar expression that evaluates to nullable
     * @return the query that returns a nullable scalar value
     */
    fun <T : Any, S : Any> select(
        expression: ScalarExpression<T, S>
    ): ScalarQuery<T?, T, S>

    /**
     * Builds a SELECT clause.
     * @param expression the scalar expression that evaluates to not null
     * @return the query that returns a non-null scalar value
     */
    fun <T : Any, S : Any> selectNotNull(
        expression: ScalarExpression<T, S>
    ): ScalarQuery<T, T, S>

    /**
     * Builds a SELECT clause.
     * @param expression the column expression that evaluates to nullable
     * @return the query that returns a list of nullable values
     */
    fun <A : Any> select(
        expression: ColumnExpression<A, *>
    ): FlowSubquery<A?>

    /**
     * Builds a SELECT clause.
     * @param expression the column expression that evaluates to not null
     * @return the query that returns a list of non-null values
     */
    fun <A : Any> selectNotNull(
        expression: ColumnExpression<A, *>
    ): FlowSubquery<A>

    /**
     * Builds a SELECT clause.
     * @param expression1 the column expression that evaluates to nullable
     * @param expression2 the column expression that evaluates to nullable
     * @return the query that returns a list of nullable value pairs
     */
    fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A?, B?>>

    /**
     * Builds a SELECT clause.
     * @param expression1 the column expression that evaluates to not null
     * @param expression2 the column expression that evaluates to not null
     * @return the query that returns a list of non-null value pairs
     */
    fun <A : Any, B : Any> selectNotNull(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A, B>>

    /**
     * Builds a SELECT clause.
     * @param expression1 the column expression that evaluates to nullable
     * @param expression2 the column expression that evaluates to nullable
     * @param expression3 the column expression that evaluates to nullable
     * @return the query that returns a list of nullable value triples
     */
    fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A?, B?, C?>>

    /**
     * Builds a SELECT clause.
     * @param expression1 the column expression that evaluates to not null
     * @param expression2 the column expression that evaluates to not null
     * @param expression3 the column expression that evaluates to not null
     * @return the query that returns a list of non-null value triples
     */
    fun <A : Any, B : Any, C : Any> selectNotNull(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A, B, C>>

    /**
     * Builds a SELECT clause.
     * @param expressions the column expressions
     * @return the query that returns a list of records
     */
    fun select(
        vararg expressions: ColumnExpression<*, *>,
    ): FlowSubquery<Record>

    /**
     * Builds a SELECT clause.
     * @param expressions the column expressions
     * @return the query that returns a list of records
     */
    fun selectAsRecord(
        vararg expressions: ColumnExpression<*, *>,
    ): FlowSubquery<Record>
}
