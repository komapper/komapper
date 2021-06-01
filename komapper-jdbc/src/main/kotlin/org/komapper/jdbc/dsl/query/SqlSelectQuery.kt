package org.komapper.jdbc.dsl.query

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
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

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
        return copy(context = newContext).first()
    }

    override fun firstOrNull(declaration: WhereDeclaration): Query<ENTITY?> {
        val newContext = support.first(declaration)
        return copy(context = newContext).firstOrNull()
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
        return Collectable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
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
        return Collectable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
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
        return Collectable(newContext, option) { dialect, rs: ResultSet ->
            val m = EntityMapper(dialect, rs)
            val map = list.associateWith { m.execute(it) }
            EntitiesImpl(map)
        }
    }

    override fun <T : Any, S : Any> select(expression: ScalarExpression<T, S>): ScalarQuery<T?, T, S> {
        val newContext = context.setProjection(expression)
        val query = Collectable(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(expression)
        }
        return ScalarQueryImpl(query, expression)
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): Subquery<A?> {
        val newContext = context.setProjection(expression)
        return Collectable(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(expression)
        }
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>
    ): Subquery<Pair<A?, B?>> {
        val newContext = context.setProjection(expression1, expression2)
        return Collectable(newContext, option) { dialect, rs ->
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
        return Collectable(newContext, option) { dialect, rs: ResultSet ->
            val m = PropertyMapper(dialect, rs)
            Triple(m.execute(expression1), m.execute(expression2), m.execute(expression3))
        }
    }

    override fun select(vararg expressions: ColumnExpression<*, *>): Subquery<Columns> {
        val list = expressions.toList()
        val newContext = context.setProjection(*list.toTypedArray())
        return Collectable(newContext, option) { dialect, rs: ResultSet ->
            val mapper = PropertyMapper(dialect, rs)
            val map = list.associateWith { mapper.execute(it) }
            ColumnsImpl(map)
        }
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val terminal = createTerminal(context) { it.toList() }
        return terminal.run(config)
    }

    override fun dryRun(config: DatabaseConfig): String {
        val terminal = createTerminal(context) { it.toList() }
        return terminal.dryRun(config)
    }

    override fun first(): Query<ENTITY> {
        val newContext = support.limit(1)
        return createTerminal(newContext) { it.first() }
    }

    override fun firstOrNull(): Query<ENTITY?> {
        val newContext = support.limit(1)
        return createTerminal(newContext) { it.firstOrNull() }
    }

    override fun <R> collect(transform: (Sequence<ENTITY>) -> R): Query<R> {
        return createTerminal(context, transform)
    }

    private fun <R> createTerminal(
        c: SqlSelectContext<ENTITY, ID, META>,
        transform: (Sequence<ENTITY>) -> R
    ): Query<R> {
        val provide: (JdbcDialect, ResultSet) -> ENTITY = { dialect, rs ->
            val mapper = EntityMapper(dialect, rs)
            val entity = mapper.execute(c.target, true)
            checkNotNull(entity)
        }
        return Terminal(c, option, provide, transform)
    }

    internal data class Collectable<T>(
        private val context: SqlSelectContext<*, *, *>,
        private val option: SqlSelectOption,
        private val provide: (JdbcDialect, ResultSet) -> T
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

        override fun run(config: DatabaseConfig): List<T> {
            val terminal = createTerminal { it.toList() }
            return terminal.run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            val terminal = createTerminal { it.toList() }
            return terminal.dryRun(config)
        }

        override fun first(): Query<T> {
            return createTerminal { it.first() }
        }

        override fun firstOrNull(): Query<T?> {
            return createTerminal { it.firstOrNull() }
        }

        override fun <R> collect(transform: (Sequence<T>) -> R): Query<R> {
            return createTerminal(transform)
        }

        private fun <R> createTerminal(transform: (Sequence<T>) -> R): Query<R> {
            return Terminal(context, option, provide, transform)
        }
    }

    internal class Terminal<T, R>(
        private val context: SqlSelectContext<*, *, *>,
        private val option: SqlSelectOption,
        private val provide: (JdbcDialect, ResultSet) -> T,
        private val transform: (Sequence<T>) -> R
    ) : Query<R> {

        override fun run(config: DatabaseConfig): R {
            if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
                error("Empty where clause is not allowed.")
            }
            val statement = buildStatement(config)
            val executor = JdbcExecutor(config, option)
            return executor.executeQuery(statement, provide, transform)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return buildStatement(config).toString()
        }

        private fun buildStatement(config: DatabaseConfig): Statement {
            val builder = SqlSelectStatementBuilder(config.dialect, context)
            return builder.build()
        }
    }
}
