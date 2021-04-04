package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationComponent
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.scope.HavingDeclaration
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.SqlSelectOptionDeclaration
import org.komapper.core.dsl.scope.SqlSelectOptionScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import java.sql.ResultSet

interface SqlSelectQuery<ENTITY> : SqlSetOperandQuery<ENTITY> {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSelectQuery<ENTITY>
    fun groupBy(vararg items: ColumnInfo<*>): SqlSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): SqlSelectQuery<ENTITY>
    fun offset(value: Int): SqlSelectQuery<ENTITY>
    fun limit(value: Int): SqlSelectQuery<ENTITY>
    fun forUpdate(): SqlSelectQuery<ENTITY>
    fun option(declaration: SqlSelectOptionDeclaration): SqlSelectQuery<ENTITY>

    fun <A> select(
        e: EntityMetamodel<A>
    ): SqlSetOperandQuery<A>

    fun <A, B> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>
    ): SqlSetOperandQuery<Pair<A, B>>

    fun <A, B, C> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>,
        e3: EntityMetamodel<C>
    ): SqlSetOperandQuery<Triple<A, B, C>>

    fun <A : Any> select(
        columnInfo: ColumnInfo<A>
    ): SqlSetOperandQuery<A>

    fun <A : Any, B : Any> select(
        c1: ColumnInfo<A>,
        c2: ColumnInfo<B>
    ): SqlSetOperandQuery<Pair<A, B>>

    fun <A : Any, B : Any, C : Any> select(
        c1: ColumnInfo<A>,
        c2: ColumnInfo<B>,
        c3: ColumnInfo<C>
    ): SqlSetOperandQuery<Triple<A, B, C>>
}

internal data class SqlSelectQueryImpl<ENTITY>(
    private val context: SqlSelectContext<ENTITY>,
    private val option: SqlSelectOption = QueryOptionImpl(allowEmptyWhereClause = true)
) :
    SqlSelectQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' metamodel is not found. Bind it to this query in advance using the from or join clause."
        }
    }

    private val support: SelectQuerySupport<ENTITY, SqlSelectContext<ENTITY>> = SelectQuerySupport(context)
    override val setOperationComponent: SqlSetOperationComponent<ENTITY> = SqlSetOperationComponent.Leaf(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.leftJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun groupBy(vararg items: ColumnInfo<*>): SqlSelectQueryImpl<ENTITY> {
        val newContext = context.copy(groupBy = items.toList())
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): SqlSelectQueryImpl<ENTITY> {
        val scope = HavingScope()
        declaration(scope)
        val newContext = context.addHaving(scope.criteria.toList())
        return copy(context = newContext)
    }

    override fun orderBy(vararg items: ColumnInfo<*>): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.orderBy(*items)
        return copy(context = newContext)
    }

    override fun offset(value: Int): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.offset(value)
        return copy(context = newContext)
    }

    override fun limit(value: Int): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.limit(value)
        return copy(context = newContext)
    }

    override fun forUpdate(): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun option(declaration: SqlSelectOptionDeclaration): SqlSelectQueryImpl<ENTITY> {
        val scope = SqlSelectOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun except(other: SqlSetOperandQuery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    override fun intersect(other: SqlSetOperandQuery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    override fun union(other: SqlSetOperandQuery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    override fun unionAll(other: SqlSetOperandQuery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        other: SqlSetOperandQuery<ENTITY>
    ): SqlSetOperationQuery<ENTITY> {
        val component = SqlSetOperationComponent.Composite(kind, setOperationComponent, other.setOperationComponent)
        val setOperatorContext = SqlSetOperationContext(component)
        return SetOperationQueryImpl(setOperatorContext) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            m.execute(context.entityMetamodel)
        }
    }

    override fun <A> select(
        e: EntityMetamodel<A>,
    ): SqlSetOperandQuery<A> {
        val entityMetamodels = context.getAliasableEntityMetamodels()
        if (entityMetamodels.none { it == e }) error(entityMetamodelNotFound("e"))
        val newContext = context.setTable(e)
        return Transformable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            m.execute(e)
        }
    }

    override fun <A, B> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>
    ): SqlSetOperandQuery<Pair<A, B>> {
        val entityMetamodels = context.getAliasableEntityMetamodels()
        if (entityMetamodels.none { it == e1 }) error(entityMetamodelNotFound("e1"))
        if (entityMetamodels.none { it == e2 }) error(entityMetamodelNotFound("e2"))
        val newContext = context.setTables(listOf(e1, e2))
        return Transformable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            m.execute(e1) to m.execute(e2)
        }
    }

    override fun <A, B, C> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>,
        e3: EntityMetamodel<C>
    ): SqlSetOperandQuery<Triple<A, B, C>> {
        val entityMetamodels = context.getAliasableEntityMetamodels()
        if (entityMetamodels.none { it == e1 }) error(entityMetamodelNotFound("e1"))
        if (entityMetamodels.none { it == e2 }) error(entityMetamodelNotFound("e2"))
        if (entityMetamodels.none { it == e3 }) error(entityMetamodelNotFound("e3"))
        val newContext = context.setTables(listOf(e1, e2, e3))
        return Transformable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            Triple(m.execute(e1), m.execute(e2), m.execute(e3))
        }
    }

    override fun <A : Any> select(columnInfo: ColumnInfo<A>): SqlSetOperandQuery<A> {
        val newContext = context.setColumn(columnInfo)
        return Transformable(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(columnInfo)
        }
    }

    override fun <A : Any, B : Any> select(c1: ColumnInfo<A>, c2: ColumnInfo<B>): SqlSetOperandQuery<Pair<A, B>> {
        val newContext = context.setColumns(listOf(c1, c2))
        return Transformable(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(c1) to m.execute(c2)
        }
    }

    override fun <A : Any, B : Any, C : Any> select(
        c1: ColumnInfo<A>,
        c2: ColumnInfo<B>,
        c3: ColumnInfo<C>
    ): SqlSetOperandQuery<Triple<A, B, C>> {
        val newContext = context.setColumns(listOf(c1, c2, c3))
        return Transformable(newContext, option) { dialect, rs: ResultSet ->
            val m = PropertyMapper(dialect, rs)
            Triple(m.execute(c1), m.execute(c2), m.execute(c3))
        }
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val terminal = createTerminal(context) { it.toList() }
        return terminal.run(config)
    }

    override fun dryRun(dialect: Dialect): Statement {
        val terminal = createTerminal(context) { it.toList() }
        return terminal.dryRun(dialect)
    }

    override fun first(): Query<ENTITY> {
        val newContext = support.limit(1)
        return createTerminal(newContext) { it.first() }
    }

    override fun firstOrNull(): Query<ENTITY?> {
        val newContext = support.limit(1)
        return createTerminal(newContext) { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<ENTITY>) -> R): Query<R> {
        return createTerminal(context, transformer)
    }

    private fun <R> createTerminal(c: SqlSelectContext<ENTITY>, transformer: (Sequence<ENTITY>) -> R): Query<R> {
        val provider: (Dialect, ResultSet) -> ENTITY = { dialect, rs ->
            val mapper = EntityMapper(dialect, rs)
            mapper.execute(c.entityMetamodel)
        }
        return Terminal(c, option, provider, transformer)
    }

    private data class Transformable<T>(
        private val context: SqlSelectContext<*>,
        private val option: SqlSelectOption,
        private val provider: (Dialect, ResultSet) -> T
    ) : SqlSetOperandQuery<T> {

        override val setOperationComponent = SqlSetOperationComponent.Leaf<T>(context)

        override fun except(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.EXCEPT, other)
        }

        override fun intersect(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.INTERSECT, other)
        }

        override fun union(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.UNION, other)
        }

        override fun unionAll(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.UNION_ALL, other)
        }

        private fun setOperation(kind: SqlSetOperationKind, other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            val component = SqlSetOperationComponent.Composite(kind, setOperationComponent, other.setOperationComponent)
            val context = SqlSetOperationContext(component)
            return SetOperationQueryImpl(context, provider = provider)
        }

        override fun run(config: DatabaseConfig): List<T> {
            val terminal = createTerminal { it.toList() }
            return terminal.run(config)
        }

        override fun dryRun(dialect: Dialect): Statement {
            val terminal = createTerminal { it.toList() }
            return terminal.dryRun(dialect)
        }

        override fun first(): Query<T> {
            return createTerminal { it.first() }
        }

        override fun firstOrNull(): Query<T?> {
            return createTerminal { it.firstOrNull() }
        }

        override fun <R> transform(transformer: (Sequence<T>) -> R): Query<R> {
            return createTerminal(transformer)
        }

        private fun <R> createTerminal(transformer: (Sequence<T>) -> R): Query<R> {
            return Terminal(context, option, provider, transformer)
        }
    }

    private class Terminal<T, R>(
        private val context: SqlSelectContext<*>,
        private val option: SqlSelectOption,
        private val provider: (Dialect, ResultSet) -> T,
        val transformer: (Sequence<T>) -> R
    ) : Query<R> {

        override fun run(config: DatabaseConfig): R {
            if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
                error("Empty where clause is not allowed.")
            }
            val statement = buildStatement(config.dialect)
            val executor = JdbcExecutor(config, option.asJdbcOption())
            return executor.executeQuery(statement, provider, transformer)
        }

        override fun dryRun(dialect: Dialect): Statement {
            return buildStatement(dialect)
        }

        private fun buildStatement(dialect: Dialect): Statement {
            val builder = SqlSelectStatementBuilder(dialect, context)
            return builder.build()
        }
    }
}
