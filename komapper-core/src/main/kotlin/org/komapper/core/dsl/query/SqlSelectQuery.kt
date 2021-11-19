package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.HavingDeclaration
import org.komapper.core.dsl.expression.OnDeclaration
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

interface SqlSelectQuery<ENTITY : Any> : FlowSubquery<ENTITY> {

    fun distinct(): SqlSelectQuery<ENTITY>
    fun <ENTITY2 : Any, ID2, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(
        metamodel: META2,
        on: OnDeclaration
    ): SqlSelectQuery<ENTITY>

    fun <ENTITY2 : Any, ID2, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(
        metamodel: META2,
        on: OnDeclaration
    ): SqlSelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSelectQuery<ENTITY>
    fun groupBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY>
    fun orderBy(vararg expressions: SortExpression): SqlSelectQuery<ENTITY>
    fun offset(offset: Int): SqlSelectQuery<ENTITY>
    fun limit(limit: Int): SqlSelectQuery<ENTITY>
    fun forUpdate(): SqlSelectQuery<ENTITY>
    fun options(configure: (SqlSelectOptions) -> SqlSelectOptions): SqlSelectQuery<ENTITY>

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

internal data class SqlSelectQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    override val context: SqlSelectContext<ENTITY, ID, META>,
    private val options: SqlSelectOptions = SqlSelectOptions.default
) :
    SqlSelectQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, ID, META, SqlSelectContext<ENTITY, ID, META>> =
        SelectQuerySupport(context)

    private val subquerySupport: FlowSubquerySupport<ENTITY> =
        FlowSubquerySupport(context) { SqlSetOperationQueryImpl(it, metamodel = context.target) }

    override fun distinct(): SqlSelectQuery<ENTITY> {
        val newContext = context.copy(distinct = true)
        return copy(context = newContext)
    }

    override fun <ENTITY2 : Any, ID2, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(
        metamodel: META2,
        on: OnDeclaration
    ): SqlSelectQuery<ENTITY> {
        val newContext = support.innerJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun <ENTITY2 : Any, ID2, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(
        metamodel: META2,
        on: OnDeclaration
    ): SqlSelectQuery<ENTITY> {
        val newContext = support.leftJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlSelectQuery<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun groupBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQuery<ENTITY> {
        val newContext = context.copy(groupBy = context.groupBy + expressions)
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY> {
        val newContext = context.copy(having = context.having + declaration)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: SortExpression): SqlSelectQuery<ENTITY> {
        val newContext = support.orderBy(*expressions)
        return copy(context = newContext)
    }

    override fun offset(offset: Int): SqlSelectQuery<ENTITY> {
        val newContext = support.offset(offset)
        return copy(context = newContext)
    }

    override fun limit(limit: Int): SqlSelectQuery<ENTITY> {
        val newContext = support.limit(limit)
        return copy(context = newContext)
    }

    override fun forUpdate(): SqlSelectQuery<ENTITY> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun options(configure: (SqlSelectOptions) -> SqlSelectOptions): SqlSelectQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSelectQuery(context, options) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSelectQuery(context, options)
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlSelectQuery(context, options, collect)
        }
    }

    override fun except(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return subquerySupport.except(other)
    }

    override fun intersect(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return subquerySupport.intersect(other)
    }

    override fun union(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return subquerySupport.union(other)
    }

    override fun unionAll(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return subquerySupport.unionAll(other)
    }

    override fun <T : Any, S : Any> select(expression: ScalarExpression<T, S>): ScalarQuery<T?, T, S> {
        val newContext = context.setProjection(expression)
        val query = SqlSingleColumnQuery(newContext, options, expression)
        return ScalarQueryImpl(query, expression)
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): FlowSubquery<A?> {
        val newContext = context.setProjection(expression)
        return SqlSingleColumnQuery(newContext, options, expression)
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A?, B?>> {
        val newContext = context.setProjection(expression1, expression2)
        return SqlPairColumnsQuery(newContext, options, expression1 to expression2)
    }

    override fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A?, B?, C?>> {
        val newContext = context.setProjection(expression1, expression2, expression3)
        return SqlTripleColumnsQuery(newContext, options, Triple(expression1, expression2, expression3))
    }

    override fun select(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Columns> {
        return selectColumns(*expressions)
    }

    override fun selectColumns(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Columns> {
        val list = expressions.toList()
        val newContext = context.setProjection(*list.toTypedArray())
        return SqlMultipleColumnsQuery(newContext, options, list)
    }
}
