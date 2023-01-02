package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.InnerJoin
import org.komapper.core.dsl.element.LeftJoin
import org.komapper.core.dsl.element.Relationship
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ForUpdateDeclaration
import org.komapper.core.dsl.expression.HavingDeclaration
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to retrieve rows.
 */
interface RelationSelectQuery<ENTITY : Any> : SelectQuery<ENTITY, RelationSelectQuery<ENTITY>>

internal data class RelationSelectQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    override val context: SelectContext<ENTITY, ID, META>,
) :
    RelationSelectQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, ID, META> = SelectQuerySupport(context)

    private val subquerySupport: FlowSubquerySupport<ENTITY> =
        FlowSubquerySupport(context) { RelationSetOperationQueryImpl(it, metamodel = context.target) }

    override fun distinct(): RelationSelectQuery<ENTITY> {
        val newContext = context.copy(distinct = true)
        return copy(context = newContext)
    }

    override fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(relationship: Relationship<ENTITY2, ID2, META2>): RelationSelectQuery<ENTITY> {
        val newContext = support.join(InnerJoin(relationship.metamodel, relationship.on))
        return copy(context = newContext)
    }

    override fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(relationship: Relationship<ENTITY2, ID2, META2>): RelationSelectQuery<ENTITY> {
        val newContext = support.join(LeftJoin(relationship.metamodel, relationship.on))
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): RelationSelectQuery<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun groupBy(expressions: List<ColumnExpression<*, *>>): RelationSelectQuery<ENTITY> {
        val newContext = context.copy(groupBy = context.groupBy + expressions)
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): RelationSelectQuery<ENTITY> {
        val newContext = context.copy(having = context.having + declaration)
        return copy(context = newContext)
    }

    override fun orderBy(expressions: List<SortExpression>): RelationSelectQuery<ENTITY> {
        val newContext = support.orderBy(expressions)
        return copy(context = newContext)
    }

    override fun offset(offset: Int): RelationSelectQuery<ENTITY> {
        val newContext = support.offset(offset)
        return copy(context = newContext)
    }

    override fun limit(limit: Int): RelationSelectQuery<ENTITY> {
        val newContext = support.limit(limit)
        return copy(context = newContext)
    }

    override fun forUpdate(declaration: ForUpdateDeclaration): RelationSelectQuery<ENTITY> {
        val newContext = support.forUpdate(declaration)
        return copy(context = newContext)
    }

    override fun options(configure: (SelectOptions) -> SelectOptions): RelationSelectQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationSelectQuery(context)
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.relationSelectQuery(context, collect)
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
        val newContext = support.select(expression)
        val query = SingleColumnSelectQuery(newContext, expression)
        return NullableScalarQuery(query, expression)
    }

    override fun <T : Any, S : Any> selectNotNull(expression: ScalarExpression<T, S>): ScalarQuery<T, T, S> {
        val newContext = support.select(expression)
        val query = SingleNotNullColumnSelectQuery(newContext, expression)
        return NotNullScalarQuery(query, expression)
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): FlowSubquery<A?> {
        val newContext = support.select(expression)
        return SingleColumnSelectQuery(newContext, expression)
    }

    override fun <A : Any> selectNotNull(expression: ColumnExpression<A, *>): FlowSubquery<A> {
        val newContext = support.select(expression)
        return SingleNotNullColumnSelectQuery(newContext, expression)
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
    ): FlowSubquery<Pair<A?, B?>> {
        val newContext = support.select(expression1, expression2)
        return PairColumnsSelectQuery(newContext, expression1 to expression2)
    }

    override fun <A : Any, B : Any> selectNotNull(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
    ): FlowSubquery<Pair<A, B>> {
        val newContext = support.select(expression1, expression2)
        return PairNotNullColumnsSelectQuery(newContext, expression1 to expression2)
    }

    override fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>,
    ): FlowSubquery<Triple<A?, B?, C?>> {
        val newContext = support.select(expression1, expression2, expression3)
        return TripleColumnsSelectQuery(newContext, Triple(expression1, expression2, expression3))
    }

    override fun <A : Any, B : Any, C : Any> selectNotNull(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>,
    ): FlowSubquery<Triple<A, B, C>> {
        val newContext = support.select(expression1, expression2, expression3)
        return TripleNotNullColumnsSelectQuery(newContext, Triple(expression1, expression2, expression3))
    }

    override fun select(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Record> {
        return selectAsRecord(*expressions)
    }

    override fun selectAsRecord(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Record> {
        val list = expressions.toList()
        val newContext = support.select(*list.toTypedArray())
        return MultipleColumnsSelectQuery(newContext, list)
    }
}
