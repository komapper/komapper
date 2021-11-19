package org.komapper.core.dsl.query

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.HavingDeclaration
import org.komapper.core.dsl.expression.OnDeclaration
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions

interface SelectQuery<ENTITY : Any, OPTIONS : SelectOptions, QUERY : SelectQuery<ENTITY, OPTIONS, QUERY>> :
    FlowSubquery<ENTITY> {
    fun distinct(): SelectQuery<ENTITY, OPTIONS, QUERY>
    fun <ENTITY2 : Any, ID2, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(
        metamodel: META2,
        on: OnDeclaration
    ): QUERY

    fun <ENTITY2 : Any, ID2, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(
        metamodel: META2,
        on: OnDeclaration
    ): QUERY

    fun where(declaration: WhereDeclaration): QUERY
    fun orderBy(vararg expressions: SortExpression): QUERY
    fun offset(offset: Int): QUERY
    fun limit(limit: Int): QUERY
    fun forUpdate(): QUERY
    fun options(configure: (OPTIONS) -> OPTIONS): QUERY
    fun groupBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY>
    fun <T : Any, S : Any> select(
        expression: ScalarExpression<T, S>
    ): ScalarQuery<T?, T, S>

    fun <A : Any> select(
        expression: ColumnExpression<A, *>
    ): FlowSubquery<A?>

    fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A?, B?>>

    fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A?, B?, C?>>

    fun select(
        vararg expressions: ColumnExpression<*, *>,
    ): FlowSubquery<Columns>

    fun selectColumns(
        vararg expressions: ColumnExpression<*, *>,
    ): FlowSubquery<Columns>
}
