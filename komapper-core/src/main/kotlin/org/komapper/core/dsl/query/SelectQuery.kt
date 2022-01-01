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
 */
interface SelectQuery<ENTITY : Any, QUERY : SelectQuery<ENTITY, QUERY>> :
    FlowSubquery<ENTITY> {
    fun distinct(): QUERY
    fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(
        metamodel: META2,
        on: OnDeclaration
    ): QUERY = innerJoin(Relationship(metamodel, on))

    fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(
        metamodel: META2,
        on: OnDeclaration
    ): QUERY = leftJoin(Relationship(metamodel, on))

    fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(
        relationship: Relationship<ENTITY2, ID2, META2>,
    ): QUERY

    fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(
        relationship: Relationship<ENTITY2, ID2, META2>,
    ): QUERY

    fun where(declaration: WhereDeclaration): QUERY
    fun orderBy(vararg expressions: SortExpression): QUERY = orderBy(expressions.toList())
    fun orderBy(expressions: List<SortExpression>): QUERY
    fun offset(offset: Int): QUERY
    fun limit(limit: Int): QUERY
    fun forUpdate(): QUERY
    fun options(configure: (SelectOptions) -> SelectOptions): QUERY
    fun groupBy(vararg expressions: ColumnExpression<*, *>): RelationSelectQuery<ENTITY> = groupBy(expressions.toList())
    fun groupBy(expressions: List<ColumnExpression<*, *>>): RelationSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): RelationSelectQuery<ENTITY>
    fun <T : Any, S : Any> select(
        expression: ScalarExpression<T, S>
    ): ScalarQuery<T?, T, S>

    fun <T : Any, S : Any> selectNotNull(
        expression: ScalarExpression<T, S>
    ): ScalarQuery<T, T, S>

    fun <A : Any> select(
        expression: ColumnExpression<A, *>
    ): FlowSubquery<A?>

    fun <A : Any> selectNotNull(
        expression: ColumnExpression<A, *>
    ): FlowSubquery<A>

    fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A?, B?>>

    fun <A : Any, B : Any> selectNotNull(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A, B>>

    fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A?, B?, C?>>

    fun <A : Any, B : Any, C : Any> selectNotNull(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A, B, C>>

    fun select(
        vararg expressions: ColumnExpression<*, *>,
    ): FlowSubquery<Columns>

    fun selectColumns(
        vararg expressions: ColumnExpression<*, *>,
    ): FlowSubquery<Columns>
}
