package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.scope.HavingDeclaration
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration

interface SqlSelectQuery<ENTITY : Any> : Subquery<ENTITY>, FlowableQuery<ENTITY> {

    fun distinct(): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    // TODO
    fun first(declaration: WhereDeclaration): Query<ENTITY>
    fun firstOrNull(declaration: WhereDeclaration): Query<ENTITY?>
    fun where(declaration: WhereDeclaration): SqlSelectQuery<ENTITY>
    fun groupBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY>
    fun orderBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQuery<ENTITY>
    fun offset(offset: Int): SqlSelectQuery<ENTITY>
    fun limit(limit: Int): SqlSelectQuery<ENTITY>
    fun forUpdate(): SqlSelectQuery<ENTITY>
    fun option(configure: (SqlSelectOption) -> SqlSelectOption): SqlSelectQuery<ENTITY>

    fun <B : Any, B_META : EntityMetamodel<B, *, B_META>> select(
        metamodel: B_META
    ): Subquery<Pair<ENTITY, B?>>

    fun <B : Any, B_META : EntityMetamodel<B, *, B_META>,
        C : Any, C_META : EntityMetamodel<C, *, C_META>> select(
        metamodel1: B_META,
        metamodel2: C_META
    ): Subquery<Triple<ENTITY, B?, C?>>

    fun select(
        vararg metamodels: EntityMetamodel<*, *, *>,
    ): Subquery<Entities>

    fun <T : Any, S : Any> select(
        expression: ScalarExpression<T, S>
    ): ScalarQuery<T?, T, S>

    fun <A : Any> select(
        expression: ColumnExpression<A, *>
    ): Subquery<A?>

    fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): Subquery<Pair<A?, B?>>

    fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): Subquery<Triple<A?, B?, C?>>

    fun select(
        vararg expressions: ColumnExpression<*, *>,
    ): Subquery<Columns>
}

data class SqlSelectQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: SqlSelectContext<ENTITY, ID, META>,
    val option: SqlSelectOption = SqlSelectOption.default
) :
    SqlSelectQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' metamodel is not found. Bind it to this query in advance using the from or join clause."
        }

        fun entityMetamodelNotFound(parameterName: String, index: Int): String {
            return "The '$parameterName' metamodel(index=$index) is not found. Bind it to this query in advance using the from or join clause."
        }
    }

    private val support: SelectQuerySupport<ENTITY, ID, META, SqlSelectContext<ENTITY, ID, META>> =
        SelectQuerySupport(context)

    override val subqueryContext = SubqueryContext.SqlSelect<ENTITY>(context)

    override fun distinct(): SqlSelectQueryImpl<ENTITY, ID, META> {
        val newContext = context.copy(distinct = true)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.innerJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.leftJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun first(declaration: WhereDeclaration): Query<ENTITY> {
        val newContext = support.first(declaration)
        val query = copy(context = newContext)
        return query.first()
    }

    override fun firstOrNull(declaration: WhereDeclaration): Query<ENTITY?> {
        val newContext = support.first(declaration)
        val query = copy(context = newContext)
        return query.firstOrNull()
    }

    override fun where(declaration: WhereDeclaration): SqlSelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun groupBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQueryImpl<ENTITY, ID, META> {
        val newContext = context.copy(groupBy = context.groupBy + expressions)
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): SqlSelectQueryImpl<ENTITY, ID, META> {
        val scope = HavingScope().apply(declaration)
        val newContext = context.copy(having = context.having + scope)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.orderBy(*expressions)
        return copy(context = newContext)
    }

    override fun offset(offset: Int): SqlSelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.offset(offset)
        return copy(context = newContext)
    }

    override fun limit(limit: Int): SqlSelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.limit(limit)
        return copy(context = newContext)
    }

    override fun forUpdate(): SqlSelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun option(configure: (SqlSelectOption) -> SqlSelectOption): SqlSelectQueryImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override fun first(): Query<ENTITY> {
        return Collect(context, option) { it.first() }
    }

    override fun firstOrNull(): Query<ENTITY?> {
        return Collect(context, option) { it.firstOrNull() }
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> {
        return Collect(context, option, collect)
    }

    override fun except(other: Subquery<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.EXCEPT, this, other)
    }

    override fun intersect(other: Subquery<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.INTERSECT, this, other)
    }

    override fun union(other: Subquery<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION, this, other)
    }

    override fun unionAll(other: Subquery<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION_ALL, this, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        left: Subquery<ENTITY>,
        right: Subquery<ENTITY>
    ): SqlSetOperationQuery<ENTITY> {
        val setOperatorContext = SqlSetOperationContext(kind, left.subqueryContext, right.subqueryContext)
        return SqlSetOperationQueryImpl(setOperatorContext, metamodel = context.target)
    }

    override fun <B : Any, B_META : EntityMetamodel<B, *, B_META>> select(
        metamodel: B_META,
    ): Subquery<Pair<ENTITY, B?>> {
        val metamodels = context.getEntityMetamodels()
        if (metamodel !in metamodels) error(entityMetamodelNotFound("metamodel"))
        val newContext = context.setProjection(context.target, metamodel)
        return SqlPairEntitiesQuery(newContext, option, context.target to metamodel)
    }

    override fun <B : Any, B_META : EntityMetamodel<B, *, B_META>,
        C : Any, C_META : EntityMetamodel<C, *, C_META>> select(
        metamodel1: B_META,
        metamodel2: C_META
    ): Subquery<Triple<ENTITY, B?, C?>> {
        val metamodels = context.getEntityMetamodels()
        if (metamodel1 !in metamodels) error(entityMetamodelNotFound("metamodel1"))
        if (metamodel2 !in metamodels) error(entityMetamodelNotFound("metamodel2"))
        val newContext = context.setProjection(context.target, metamodel1, metamodel2)
        return SqlTripleEntitiesQuery(newContext, option, Triple(context.target, metamodel1, metamodel2))
    }

    override fun select(vararg metamodels: EntityMetamodel<*, *, *>): Subquery<Entities> {
        val contextModels = context.getEntityMetamodels()
        for ((i, metamodel) in metamodels.withIndex()) {
            if (metamodel !in contextModels) error(entityMetamodelNotFound("metamodels", i))
        }
        val list = listOf(context.target) + metamodels.toList()
        val newContext = context.setProjection(*list.toTypedArray())
        return SqlMultipleEntitiesQuery(newContext, option, list)
    }

    override fun <T : Any, S : Any> select(expression: ScalarExpression<T, S>): ScalarQuery<T?, T, S> {
        val newContext = context.setProjection(expression)
        val query = SqlSingleColumnQuery(newContext, option, expression)
        return ScalarQueryImpl(query, expression)
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): Subquery<A?> {
        val newContext = context.setProjection(expression)
        return SqlSingleColumnQuery(newContext, option, expression)
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): Subquery<Pair<A?, B?>> {
        val newContext = context.setProjection(expression1, expression2)
        return SqlPairColumnsQuery(newContext, option, expression1 to expression2)
    }

    override fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): Subquery<Triple<A?, B?, C?>> {
        val newContext = context.setProjection(expression1, expression2, expression3)
        return SqlTripleColumnsQuery(newContext, option, Triple(expression1, expression2, expression3))
    }

    override fun select(vararg expressions: ColumnExpression<*, *>): Subquery<Columns> {
        val list = expressions.toList()
        val newContext = context.setProjection(*list.toTypedArray())
        return SqlMultipleColumnsQuery(newContext, option, list)
    }

    override fun toFlowQuery(): FlowQuery<ENTITY> {
        return FlowQueryImpl(context, option)
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    class Collect<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>(
        val context: SqlSelectContext<ENTITY, ID, META>,
        val option: SqlSelectOption,
        val transform: suspend (Flow<ENTITY>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }

    class FlowQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        val context: SqlSelectContext<ENTITY, ID, META>,
        val option: SqlSelectOption
    ) : FlowQuery<ENTITY> {

        override fun accept(visitor: FlowQueryVisitor): FlowQueryRunner {
            return visitor.visit(this)
        }
    }
}
