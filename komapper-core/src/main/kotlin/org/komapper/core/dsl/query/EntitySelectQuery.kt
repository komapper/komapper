package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.declaration.HavingDeclaration
import org.komapper.core.dsl.declaration.OnDeclaration
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntitySelectQuery<ENTITY : Any> : FlowSubquery<ENTITY> {

    fun distinct(): EntitySelectQuery<ENTITY>
    fun innerJoin(metamodel: EntityMetamodel<*, *, *>, on: OnDeclaration): EntitySelectQuery<ENTITY>
    fun leftJoin(metamodel: EntityMetamodel<*, *, *>, on: OnDeclaration): EntitySelectQuery<ENTITY>
    fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY>
    fun orderBy(vararg expressions: SortExpression): EntitySelectQuery<ENTITY>
    fun offset(offset: Int): EntitySelectQuery<ENTITY>
    fun limit(limit: Int): EntitySelectQuery<ENTITY>
    fun forUpdate(): EntitySelectQuery<ENTITY>
    fun options(configure: (EntitySelectOptions) -> EntitySelectOptions): EntitySelectQuery<ENTITY>

    fun include(metamodel: EntityMetamodel<*, *, *>): EntityContextQuery<ENTITY>
    fun includeAll(): EntityContextQuery<ENTITY>

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

internal data class EntitySelectQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    override val context: EntitySelectContext<ENTITY, ID, META>,
    private val options: EntitySelectOptions = EntitySelectOptions.default
) :
    EntitySelectQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, ID, META, EntitySelectContext<ENTITY, ID, META>> =
        SelectQuerySupport(context)

    override fun distinct(): EntitySelectQuery<ENTITY> {
        val newContext = context.copy(distinct = true)
        return copy(context = newContext)
    }

    override fun innerJoin(metamodel: EntityMetamodel<*, *, *>, on: OnDeclaration): EntitySelectQuery<ENTITY> {
        val newContext = support.innerJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun leftJoin(metamodel: EntityMetamodel<*, *, *>, on: OnDeclaration): EntitySelectQuery<ENTITY> {
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

    override fun options(configure: (EntitySelectOptions) -> EntitySelectOptions): EntitySelectQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun include(metamodel: EntityMetamodel<*, *, *>): EntityContextQuery<ENTITY> {
        return EntityContextQueryImpl(context, options).include(metamodel)
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
        return setOperation(SqlSetOperationKind.EXCEPT, this, other)
    }

    override fun intersect(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.INTERSECT, this, other)
    }

    override fun union(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION, this, other)
    }

    override fun unionAll(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION_ALL, this, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        left: SubqueryExpression<ENTITY>,
        right: SubqueryExpression<ENTITY>
    ): SqlSetOperationQuery<ENTITY> {
        val setOperatorContext = SqlSetOperationContext(kind, left.context, right.context)
        return SqlSetOperationQueryImpl(setOperatorContext, metamodel = context.target)
    }

    override fun groupBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQuery<ENTITY> {
        return asSqlQuery().groupBy(*expressions)
    }

    override fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY> {
        return asSqlQuery().having(declaration)
    }

    override fun <T : Any, S : Any> select(expression: ScalarExpression<T, S>): ScalarQuery<T?, T, S> {
        return asSqlQuery().select(expression)
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): FlowSubquery<A?> {
        return asSqlQuery().select(expression)
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): FlowSubquery<Pair<A?, B?>> {
        return asSqlQuery().select(expression1, expression2)
    }

    override fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): FlowSubquery<Triple<A?, B?, C?>> {
        return asSqlQuery().select(expression1, expression2, expression3)
    }

    override fun select(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Columns> {
        return asSqlQuery().select(*expressions)
    }

    override fun selectColumns(vararg expressions: ColumnExpression<*, *>): FlowSubquery<Columns> {
        return asSqlQuery().selectColumns(*expressions)
    }

    private fun asSqlQuery(): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(context.asSqlSelectContext(), options.asSqlSelectOption())
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entitySelectQuery(context, options) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return asSqlQuery().accept(visitor)
    }
}
