package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.declaration.HavingDeclaration
import org.komapper.core.dsl.declaration.OnDeclaration
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

interface SqlSelectQuery<ENTITY : Any> : FlowSubquery<ENTITY> {

    fun distinct(): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration
    ): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration
    ): SqlSelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSelectQuery<ENTITY>
    fun groupBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY>
    fun orderBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQuery<ENTITY>
    fun offset(offset: Int): SqlSelectQuery<ENTITY>
    fun limit(limit: Int): SqlSelectQuery<ENTITY>
    fun forUpdate(): SqlSelectQuery<ENTITY>
    fun options(configure: (SqlSelectOptions) -> SqlSelectOptions): SqlSelectQuery<ENTITY>

    fun <B : Any, B_META : EntityMetamodel<B, *, B_META>> select(
        metamodel: B_META
    ): FlowSubquery<Pair<ENTITY, B?>>

    fun <B : Any, B_META : EntityMetamodel<B, *, B_META>,
        C : Any, C_META : EntityMetamodel<C, *, C_META>> select(
        metamodel1: B_META,
        metamodel2: C_META
    ): FlowSubquery<Triple<ENTITY, B?, C?>>

    fun select(
        vararg metamodels: EntityMetamodel<*, *, *>,
    ): FlowSubquery<Entities>

    fun selectEntities(
        vararg metamodels: EntityMetamodel<*, *, *>,
    ): FlowSubquery<Entities>

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
    private val context: SqlSelectContext<ENTITY, ID, META>,
    private val options: SqlSelectOptions = SqlSelectOptions.default
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

    private val subquerySupport: FlowSubquerySupport<ENTITY> =
        FlowSubquerySupport(subqueryContext) { SqlSetOperationQueryImpl(it, metamodel = context.target) }

    override fun distinct(): SqlSelectQuery<ENTITY> {
        val newContext = context.copy(distinct = true)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration
    ): SqlSelectQuery<ENTITY> {
        val newContext = support.innerJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
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
        val scope = HavingScope().apply(declaration)
        val newContext = context.copy(having = context.having + scope)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): SqlSelectQuery<ENTITY> {
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

    override fun except(other: Subquery<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return subquerySupport.except(other)
    }

    override fun intersect(other: Subquery<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return subquerySupport.intersect(other)
    }

    override fun union(other: Subquery<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return subquerySupport.union(other)
    }

    override fun unionAll(other: Subquery<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return subquerySupport.unionAll(other)
    }

    override fun <B : Any, B_META : EntityMetamodel<B, *, B_META>> select(
        metamodel: B_META,
    ): FlowSubquery<Pair<ENTITY, B?>> {
        val metamodels = context.getEntityMetamodels()
        if (metamodel !in metamodels) error(entityMetamodelNotFound("metamodel"))
        val newContext = context.setProjection(context.target, metamodel)
        return SqlPairEntitiesQuery(newContext, options, context.target to metamodel)
    }

    override fun <B : Any, B_META : EntityMetamodel<B, *, B_META>,
        C : Any, C_META : EntityMetamodel<C, *, C_META>> select(
        metamodel1: B_META,
        metamodel2: C_META
    ): FlowSubquery<Triple<ENTITY, B?, C?>> {
        val metamodels = context.getEntityMetamodels()
        if (metamodel1 !in metamodels) error(entityMetamodelNotFound("metamodel1"))
        if (metamodel2 !in metamodels) error(entityMetamodelNotFound("metamodel2"))
        val newContext = context.setProjection(context.target, metamodel1, metamodel2)
        return SqlTripleEntitiesQuery(newContext, options, Triple(context.target, metamodel1, metamodel2))
    }

    override fun select(vararg metamodels: EntityMetamodel<*, *, *>): FlowSubquery<Entities> {
        return selectEntities(*metamodels)
    }

    override fun selectEntities(vararg metamodels: EntityMetamodel<*, *, *>): FlowSubquery<Entities> {
        val contextModels = context.getEntityMetamodels()
        for ((i, metamodel) in metamodels.withIndex()) {
            if (metamodel !in contextModels) error(entityMetamodelNotFound("metamodels", i))
        }
        val list = listOf(context.target) + metamodels.toList()
        val newContext = context.setProjection(*list.toTypedArray())
        return SqlMultipleEntitiesQuery(newContext, options, list)
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
