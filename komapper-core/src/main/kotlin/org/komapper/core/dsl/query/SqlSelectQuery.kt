package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.Dialect
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.scope.HavingDeclaration
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration
import java.sql.ResultSet

interface SqlSelectQuery<ENTITY : Any> : Subquery<ENTITY> {

    fun distinct(): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, OTHER_META>> innerJoin(
        entityMetamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, OTHER_META>> leftJoin(
        entityMetamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSelectQuery<ENTITY>
    fun groupBy(vararg expressions: PropertyExpression<*>): SqlSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY>
    fun orderBy(vararg expressions: PropertyExpression<*>): SqlSelectQuery<ENTITY>
    fun offset(value: Int): SqlSelectQuery<ENTITY>
    fun limit(value: Int): SqlSelectQuery<ENTITY>
    fun forUpdate(): SqlSelectQuery<ENTITY>
    fun option(configurator: QueryOptionConfigurator<SqlSelectOption>): SqlSelectQuery<ENTITY>

    fun <A : Any, AM : EntityMetamodel<A, AM>> select(
        e: AM
    ): Subquery<Pair<ENTITY, A?>>

    fun <A : Any, B : Any, AM : EntityMetamodel<A, AM>, BM : EntityMetamodel<B, BM>> select(
        e1: AM,
        e2: BM
    ): Subquery<Triple<ENTITY, A?, B?>>

    fun select(
        vararg entityMetamodels: EntityMetamodel<*, *>,
    ): Subquery<EntityRecord>

    fun <A : Any> select(
        p: PropertyExpression<A>
    ): Subquery<A?>

    fun <A : Any> select(
        p: ScalarExpression<A>
    ): ScalarQuery<A?, A>

    fun <A : Any, B : Any> select(
        p1: PropertyExpression<A>,
        p2: PropertyExpression<B>
    ): Subquery<Pair<A?, B?>>

    fun <A : Any, B : Any, C : Any> select(
        p1: PropertyExpression<A>,
        p2: PropertyExpression<B>,
        p3: PropertyExpression<C>
    ): Subquery<Triple<A?, B?, C?>>

    fun select(
        vararg propertyExpressions: PropertyExpression<*>,
    ): Subquery<PropertyRecord>
}

internal data class SqlSelectQueryImpl<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    private val context: SqlSelectContext<ENTITY, META>,
    private val option: SqlSelectOption = SqlSelectOption()
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

    private val support: SelectQuerySupport<ENTITY, META, SqlSelectContext<ENTITY, META>> = SelectQuerySupport(context)

    override val subqueryContext = SubqueryContext.SqlSelect<ENTITY>(context)

    override fun distinct(): SqlSelectQueryImpl<ENTITY, META> {
        val newContext = context.copy(distinct = true)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, OTHER_META>> innerJoin(
        entityMetamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY, META> {
        val newContext = support.innerJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, OTHER_META>> leftJoin(
        entityMetamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY, META> {
        val newContext = support.leftJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlSelectQueryImpl<ENTITY, META> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun groupBy(vararg expressions: PropertyExpression<*>): SqlSelectQueryImpl<ENTITY, META> {
        val newContext = context.copy(groupBy = context.groupBy + expressions)
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): SqlSelectQueryImpl<ENTITY, META> {
        val scope = HavingScope().apply(declaration)
        val newContext = context.copy(having = context.having + scope)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: PropertyExpression<*>): SqlSelectQueryImpl<ENTITY, META> {
        val newContext = support.orderBy(*expressions)
        return copy(context = newContext)
    }

    override fun offset(value: Int): SqlSelectQueryImpl<ENTITY, META> {
        val newContext = support.offset(value)
        return copy(context = newContext)
    }

    override fun limit(value: Int): SqlSelectQueryImpl<ENTITY, META> {
        val newContext = support.limit(value)
        return copy(context = newContext)
    }

    override fun forUpdate(): SqlSelectQueryImpl<ENTITY, META> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun option(configurator: QueryOptionConfigurator<SqlSelectOption>): SqlSelectQueryImpl<ENTITY, META> {
        return copy(option = configurator.apply(option))
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

    override fun <B : Any, BM : EntityMetamodel<B, BM>> select(
        e: BM,
    ): Subquery<Pair<ENTITY, B?>> {
        val entityExpressions = context.getEntityExpressions()
        if (e !in entityExpressions) error(entityMetamodelNotFound("e"))
        val newContext = context.setEntities(context.target, e)
        return Collectable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            val base = checkNotNull(m.execute(context.target, true))
            base to m.execute(e)
        }
    }

    override fun <B : Any, C : Any, BM : EntityMetamodel<B, BM>, CM : EntityMetamodel<C, CM>> select(
        e1: BM,
        e2: CM
    ): Subquery<Triple<ENTITY, B?, C?>> {
        val entityExpressions = context.getEntityExpressions()
        if (e1 !in entityExpressions) error(entityMetamodelNotFound("e1"))
        if (e2 !in entityExpressions) error(entityMetamodelNotFound("e2"))
        val newContext = context.setEntities(context.target, e1, e2)
        return Collectable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            val base = checkNotNull(m.execute(context.target, true))
            Triple(base, m.execute(e1), m.execute(e2))
        }
    }

    override fun select(vararg entityMetamodels: EntityMetamodel<*, *>): Subquery<EntityRecord> {
        val entityExpressions = context.getEntityExpressions()
        for ((i, e) in entityMetamodels.withIndex()) {
            if (e !in entityExpressions) error(entityMetamodelNotFound("e", i))
        }
        val list = listOf(context.target) + entityMetamodels.toList()
        val newContext = context.setEntities(*list.toTypedArray())
        return Collectable(newContext, option) { dialect, rs: ResultSet ->
            val m = EntityMapper(dialect, rs)
            val map = list.associateWith { m.execute(it) }
            EntityRecordImpl(map)
        }
    }

    override fun <A : Any> select(p: PropertyExpression<A>): Subquery<A?> {
        val newContext = context.setProperties(p)
        return Collectable(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(p)
        }
    }

    override fun <A : Any> select(p: ScalarExpression<A>): ScalarQuery<A?, A> {
        val newContext = context.setProperties(p)
        val query = Collectable(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(p)
        }
        return ScalarQueryImpl(query, p)
    }

    override fun <A : Any, B : Any> select(
        p1: PropertyExpression<A>,
        p2: PropertyExpression<B>
    ): Subquery<Pair<A?, B?>> {
        val newContext = context.setProperties(p1, p2)
        return Collectable(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(p1) to m.execute(p2)
        }
    }

    override fun <A : Any, B : Any, C : Any> select(
        p1: PropertyExpression<A>,
        p2: PropertyExpression<B>,
        p3: PropertyExpression<C>
    ): Subquery<Triple<A?, B?, C?>> {
        val newContext = context.setProperties(p1, p2, p3)
        return Collectable(newContext, option) { dialect, rs: ResultSet ->
            val m = PropertyMapper(dialect, rs)
            Triple(m.execute(p1), m.execute(p2), m.execute(p3))
        }
    }

    override fun select(vararg propertyExpressions: PropertyExpression<*>): Subquery<PropertyRecord> {
        val list = propertyExpressions.toList()
        val newContext = context.setProperties(*list.toTypedArray())
        return Collectable(newContext, option) { dialect, rs: ResultSet ->
            val mapper = PropertyMapper(dialect, rs)
            val map = list.associateWith { mapper.execute(it) }
            PropertyRecordImpl(map)
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

    override fun <R> collect(transformer: (Sequence<ENTITY>) -> R): Query<R> {
        return createTerminal(context, transformer)
    }

    private fun <R> createTerminal(c: SqlSelectContext<ENTITY, META>, transformer: (Sequence<ENTITY>) -> R): Query<R> {
        val provider: (Dialect, ResultSet) -> ENTITY = { dialect, rs ->
            val mapper = EntityMapper(dialect, rs)
            val entity = mapper.execute(c.target, true)
            checkNotNull(entity)
        }
        return Terminal(c, option, provider, transformer)
    }

    internal data class Collectable<T>(
        private val context: SqlSelectContext<*, *>,
        private val option: SqlSelectOption,
        private val provider: (Dialect, ResultSet) -> T
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
            return SetOperationQueryImpl(setOperationContext, provider = provider)
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

        override fun <R> collect(transformer: (Sequence<T>) -> R): Query<R> {
            return createTerminal(transformer)
        }

        private fun <R> createTerminal(transformer: (Sequence<T>) -> R): Query<R> {
            return Terminal(context, option, provider, transformer)
        }
    }

    internal class Terminal<T, R>(
        private val context: SqlSelectContext<*, *>,
        private val option: SqlSelectOption,
        private val provider: (Dialect, ResultSet) -> T,
        val transformer: (Sequence<T>) -> R
    ) : Query<R> {

        override fun run(config: DatabaseConfig): R {
            if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
                error("Empty where clause is not allowed.")
            }
            val statement = buildStatement(config)
            val executor = JdbcExecutor(config, option.asJdbcOption())
            return executor.executeQuery(statement, provider, transformer)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return buildStatement(config).sql
        }

        private fun buildStatement(config: DatabaseConfig): Statement {
            val builder = SqlSelectStatementBuilder(config.dialect, context)
            return builder.build()
        }
    }
}
