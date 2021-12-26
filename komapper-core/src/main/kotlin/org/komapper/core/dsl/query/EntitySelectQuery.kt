package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SetOperationKind
import org.komapper.core.dsl.element.InnerJoin
import org.komapper.core.dsl.element.LeftJoin
import org.komapper.core.dsl.element.Relationship
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.HavingDeclaration
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntitySelectQuery<ENTITY : Any> : SelectQuery<ENTITY, EntitySelectQuery<ENTITY>> {
    fun include(vararg metamodels: EntityMetamodel<*, *, *>): EntityStoreQuery
    fun includeAll(): EntityStoreQuery
}

internal data class EntitySelectQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    override val context: SelectContext<ENTITY, ID, META>,
) :
    EntitySelectQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, ID, META> = SelectQuerySupport(context)

    override fun distinct(): EntitySelectQuery<ENTITY> {
        val newContext = context.copy(distinct = true)
        return copy(context = newContext)
    }

    override fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(relationship: Relationship<ENTITY2, ID2, META2>): EntitySelectQuery<ENTITY> {
        val newContext = support.join(InnerJoin(relationship.metamodel, relationship.on))
        return copy(context = newContext)
    }

    override fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(relationship: Relationship<ENTITY2, ID2, META2>): EntitySelectQuery<ENTITY> {
        val newContext = support.join(LeftJoin(relationship.metamodel, relationship.on))
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun orderBy(expressions: List<SortExpression>): EntitySelectQuery<ENTITY> {
        val newContext = support.orderBy(expressions)
        return copy(context = newContext)
    }

    override fun offset(offset: Int): EntitySelectQuery<ENTITY> {
        val newContext = support.offset(offset)
        return copy(context = newContext)
    }

    override fun limit(limit: Int): EntitySelectQuery<ENTITY> {
        val newContext = support.limit(limit)
        return copy(context = newContext)
    }

    override fun forUpdate(): EntitySelectQuery<ENTITY> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun options(configure: (SelectOptions) -> SelectOptions): EntitySelectQuery<ENTITY> {
        val newContext = support.options(configure(context.options))
        return copy(context = newContext)
    }

    override fun include(vararg metamodels: EntityMetamodel<*, *, *>): EntityStoreQuery {
        return asEntityStoreQuery().include(*metamodels)
    }

    override fun includeAll(): EntityStoreQuery {
        return asEntityStoreQuery().includeAll()
    }

    private fun asEntityStoreQuery(): EntityStoreQueryImpl<ENTITY, ID, META> {
        return EntityStoreQueryImpl(context)
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entitySelectQuery(context, collect)
        }
    }

    override fun except(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return setOperation(SetOperationKind.EXCEPT, this, other)
    }

    override fun intersect(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return setOperation(SetOperationKind.INTERSECT, this, other)
    }

    override fun union(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return setOperation(SetOperationKind.UNION, this, other)
    }

    override fun unionAll(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return setOperation(SetOperationKind.UNION_ALL, this, other)
    }

    private fun setOperation(
        kind: SetOperationKind,
        left: SubqueryExpression<ENTITY>,
        right: SubqueryExpression<ENTITY>
    ): FlowSetOperationQuery<ENTITY> {
        val setOperatorContext = SetOperationContext(kind, left.context, right.context)
        return RelationSetOperationQueryImpl(setOperatorContext, metamodel = context.target)
    }

    override fun groupBy(expressions: List<ColumnExpression<*, *>>): RelationSelectQuery<ENTITY> {
        return asRelationQuery().groupBy(expressions)
    }

    override fun having(declaration: HavingDeclaration): RelationSelectQuery<ENTITY> {
        return asRelationQuery().having(declaration)
    }

    override fun <T : Any, S : Any> select(expression: ScalarExpression<T, S>): ScalarQuery<T?, T, S> {
        return asRelationQuery().select(expression)
    }

    override fun <T : Any, S : Any> selectNotNull(expression: ScalarExpression<T, S>): ScalarQuery<T, T, S> {
        return asRelationQuery().selectNotNull(expression)
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): FlowSubquery<A?> {
        return asRelationQuery().select(expression)
    }

    override fun <A : Any> selectNotNull(expression: ColumnExpression<A, *>): FlowSubquery<A> {
        return asRelationQuery().selectNotNull(expression)
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A?, B?>> {
        return asRelationQuery().select(expression1, expression2)
    }

    override fun <A : Any, B : Any> selectNotNull(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A, B>> {
        return asRelationQuery().selectNotNull(expression1, expression2)
    }

    override fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A?, B?, C?>> {
        return asRelationQuery().select(expression1, expression2, expression3)
    }

    override fun <A : Any, B : Any, C : Any> selectNotNull(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A, B, C>> {
        return asRelationQuery().selectNotNull(expression1, expression2, expression3)
    }

    override fun select(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Columns> {
        return asRelationQuery().select(*expressions)
    }

    override fun selectColumns(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Columns> {
        return asRelationQuery().selectColumns(*expressions)
    }

    private fun asRelationQuery(): RelationSelectQuery<ENTITY> {
        return RelationSelectQueryImpl(context)
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return asRelationQuery().accept(visitor)
    }
}
