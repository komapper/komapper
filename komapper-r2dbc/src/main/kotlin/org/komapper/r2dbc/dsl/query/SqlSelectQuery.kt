package org.komapper.r2dbc.dsl.query

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
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
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

interface SqlSelectQuery<ENTITY : Any> : Subquery<ENTITY> {

    fun distinct(): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

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

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>> select(
        metamodel: A_META
    ): Subquery<Pair<ENTITY, A?>>

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>,
        B : Any, B_META : EntityMetamodel<B, *, B_META>> select(
        metamodel1: A_META,
        metamodel2: B_META
    ): Subquery<Triple<ENTITY, A?, B?>>

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

internal data class SqlSelectQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlSelectContext<ENTITY, ID, META>,
    private val option: SqlSelectOption = SqlSelectOption.default
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

    override fun except(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return support.except(this, other)
    }

    override fun intersect(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return support.intersect(this, other)
    }

    override fun union(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return support.union(this, other)
    }

    override fun unionAll(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return support.unionAll(this, other)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>> select(
        metamodel: A_META,
    ): Subquery<Pair<ENTITY, A?>> {
        val metamodels = context.getEntityMetamodels()
        if (metamodel !in metamodels) error(entityMetamodelNotFound("metamodel"))
        val newContext = context.setProjection(context.target, metamodel)
        return ProjectedQuery(newContext, option) { dialect, row ->
            val m = EntityMapper(dialect, row)
            val base = checkNotNull(m.execute(context.target, true))
            base to m.execute(metamodel)
        }
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>,
        B : Any, B_META : EntityMetamodel<B, *, B_META>> select(
        metamodel1: A_META,
        metamodel2: B_META
    ): Subquery<Triple<ENTITY, A?, B?>> {
        val metamodels = context.getEntityMetamodels()
        if (metamodel1 !in metamodels) error(entityMetamodelNotFound("metamodel1"))
        if (metamodel2 !in metamodels) error(entityMetamodelNotFound("metamodel2"))
        val newContext = context.setProjection(context.target, metamodel1, metamodel2)
        return ProjectedQuery(newContext, option) { dialect, row ->
            val m = EntityMapper(dialect, row)
            val base = checkNotNull(m.execute(context.target, true))
            Triple(base, m.execute(metamodel1), m.execute(metamodel2))
        }
    }

    override fun select(vararg metamodels: EntityMetamodel<*, *, *>): Subquery<Entities> {
        val contextModels = context.getEntityMetamodels()
        for ((i, metamodel) in metamodels.withIndex()) {
            if (metamodel !in contextModels) error(entityMetamodelNotFound("metamodels", i))
        }
        val list = listOf(context.target) + metamodels.toList()
        val newContext = context.setProjection(*list.toTypedArray())
        return ProjectedQuery(newContext, option) { dialect, row ->
            val m = EntityMapper(dialect, row)
            val map = list.associateWith { m.execute(it) }
            EntitiesImpl(map)
        }
    }

    override fun <T : Any, S : Any> select(expression: ScalarExpression<T, S>): ScalarQuery<T?, T, S> {
        val newContext = context.setProjection(expression)
        val query = ProjectedQuery(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(expression)
        }
        return ScalarQueryImpl(query, expression)
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): Subquery<A?> {
        val newContext = context.setProjection(expression)
        return ProjectedQuery(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(expression)
        }
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): Subquery<Pair<A?, B?>> {
        val newContext = context.setProjection(expression1, expression2)
        return ProjectedQuery(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(expression1) to m.execute(expression2)
        }
    }

    override fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>
    ): Subquery<Triple<A?, B?, C?>> {
        val newContext = context.setProjection(expression1, expression2, expression3)
        return ProjectedQuery(newContext, option) { dialect, row ->
            val m = PropertyMapper(dialect, row)
            Triple(m.execute(expression1), m.execute(expression2), m.execute(expression3))
        }
    }

    override fun select(vararg expressions: ColumnExpression<*, *>): Subquery<Columns> {
        val list = expressions.toList()
        val newContext = context.setProjection(*list.toTypedArray())
        return ProjectedQuery(newContext, option) { dialect, row ->
            val mapper = PropertyMapper(dialect, row)
            val map = list.associateWith { mapper.execute(it) }
            ColumnsImpl(map)
        }
    }

    override suspend fun run(config: R2dbcDatabaseConfig): Flow<ENTITY> {
        val query = createProjectedQuery()
        return query.run(config)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        val query = createProjectedQuery()
        return query.dryRun(config)
    }

    private fun createProjectedQuery(): ProjectedQuery<ENTITY> {
        return ProjectedQuery(context, option) { dialect, row ->
            val m = EntityMapper(dialect, row)
            m.execute(context.target, true) as ENTITY
        }
    }

    internal data class ProjectedQuery<T>(
        private val context: SqlSelectContext<*, *, *>,
        private val option: SqlSelectOption,
        private val provide: (R2dbcDialect, Row) -> T
    ) : Subquery<T> {

        override val subqueryContext = SubqueryContext.SqlSelect<T>(context)

        override fun except(other: Subquery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.EXCEPT, other)
        }

        override fun intersect(other: Subquery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.INTERSECT, other)
        }

        override fun union(other: Subquery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.UNION, other)
        }

        override fun unionAll(other: Subquery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.UNION_ALL, other)
        }

        private fun setOperation(kind: SqlSetOperationKind, other: Subquery<T>): SqlSetOperationQuery<T> {
            val setOperationContext = SqlSetOperationContext(kind, subqueryContext, other.subqueryContext)
            return SetOperationQueryImpl(setOperationContext, provide = provide)
        }

        override suspend fun run(config: R2dbcDatabaseConfig): Flow<T> {
            if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
                error("Empty where clause is not allowed.")
            }
            val statement = buildStatement(config)
            val executor = R2dbcExecutor(config, option)
            return executor.executeQuery(statement) { row, _ ->
                provide(config.dialect, row)
            }
        }

        override fun dryRun(config: R2dbcDatabaseConfig): String {
            return buildStatement(config).toString()
        }

        private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
            val builder = SqlSelectStatementBuilder(config.dialect, context)
            return builder.build()
        }
    }
}
