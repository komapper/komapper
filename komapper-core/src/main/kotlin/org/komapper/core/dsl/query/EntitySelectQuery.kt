package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SetOperationKind
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.HavingDeclaration
import org.komapper.core.dsl.expression.OnDeclaration
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntitySelectQuery<ENTITY : Any> : SelectQuery<ENTITY, EntitySelectQuery<ENTITY>> {
    fun include(vararg metamodels: EntityMetamodel<*, *, *>): EntityContextQuery<ENTITY>
    fun includeAll(): EntityContextQuery<ENTITY>
}

internal data class EntitySelectQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    override val context: SelectContext<ENTITY, ID, META>,
    private val options: SelectOptions = SelectOptions.default
) :
    EntitySelectQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, ID, META> = SelectQuerySupport(context)

    override fun distinct(): EntitySelectQuery<ENTITY> {
        val newContext = context.copy(distinct = true)
        return copy(context = newContext)
    }

    override fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(
        metamodel: META2,
        on: OnDeclaration
    ): EntitySelectQuery<ENTITY> {
        val newContext = support.innerJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun <ENTITY2 : Any, ID2 : Any, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(
        metamodel: META2,
        on: OnDeclaration
    ): EntitySelectQuery<ENTITY> {
        val newContext = support.leftJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: SortExpression): EntitySelectQuery<ENTITY> {
        val newContext = support.orderBy(*expressions)
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
        return copy(options = configure(options))
    }

    override fun include(vararg metamodels: EntityMetamodel<*, *, *>): EntityContextQuery<ENTITY> {
        return EntityContextQueryImpl(context, options).include(*metamodels)
    }

    override fun includeAll(): EntityContextQuery<ENTITY> {
        return EntityContextQueryImpl(context, options).includeAll()
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entitySelectQuery(context, options, collect)
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

    override fun groupBy(vararg expressions: ColumnExpression<*, *>): RelationSelectQuery<ENTITY> {
        return asRelationQuery().groupBy(*expressions)
    }

    override fun having(declaration: HavingDeclaration): RelationSelectQuery<ENTITY> {
        return asRelationQuery().having(declaration)
    }

    override fun <T : Any, S : Any> select(expression: ScalarExpression<T, S>): ScalarQuery<T?, T, S> {
        return asRelationQuery().select(expression)
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): FlowSubquery<A?> {
        return asRelationQuery().select(expression)
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A?, B?>> {
        return asRelationQuery().select(expression1, expression2)
    }

    override fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A?, B?, C?>> {
        return asRelationQuery().select(expression1, expression2, expression3)
    }

    override fun select(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Columns> {
        return asRelationQuery().select(*expressions)
    }

    override fun selectColumns(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Columns> {
        return asRelationQuery().selectColumns(*expressions)
    }

    private fun asRelationQuery(): RelationSelectQuery<ENTITY> {
        return RelationSelectQueryImpl(context, options)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entitySelectQuery(context, options) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return asRelationQuery().accept(visitor)
    }
}
