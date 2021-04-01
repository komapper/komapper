package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.scope.HavingDeclaration
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.SqlSelectOptionsDeclaration
import org.komapper.core.dsl.scope.SqlSelectOptionsScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import java.sql.ResultSet

interface SqlSelectQuery<ENTITY> : ListQuery<ENTITY> {

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
    fun options(declaration: SqlSelectOptionsDeclaration): SqlSelectQuery<ENTITY>

    fun <A, B> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>
    ): ListQuery<Pair<A, B>>

    fun <A, B, C> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>,
        e3: EntityMetamodel<C>
    ): ListQuery<Triple<A, B, C>>

    fun <A : Any> select(
        columnInfo: ColumnInfo<A>
    ): ListQuery<A>

    fun <A : Any, B : Any> select(
        c1: ColumnInfo<A>,
        c2: ColumnInfo<B>
    ): ListQuery<Pair<A, B>>

    fun <A : Any, B : Any, C : Any> select(
        c1: ColumnInfo<A>,
        c2: ColumnInfo<B>,
        c3: ColumnInfo<C>
    ): ListQuery<Triple<A, B, C>>
}

internal data class SqlSelectQueryImpl<ENTITY>(
    private val context: SqlSelectContext<ENTITY>
) :
    SqlSelectQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, SqlSelectContext<ENTITY>> = SelectQuerySupport(context)

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

    override fun options(declaration: SqlSelectOptionsDeclaration): SqlSelectQuery<ENTITY> {
        val scope = SqlSelectOptionsScope(context.options)
        declaration(scope)
        val newContext = context.copy(options = scope.options)
        return copy(context = newContext)
    }

    override fun <A, B> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>
    ): ListQuery<Pair<A, B>> {
        // TODO check
        val newContext = context.setTables(listOf(e1, e2))
        return Transformable(newContext) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            m.execute(e1) to m.execute(e2)
        }
    }

    override fun <A, B, C> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>,
        e3: EntityMetamodel<C>
    ): ListQuery<Triple<A, B, C>> {
        // TODO check
        val newContext = context.setTables(listOf(e1, e2, e3))
        return Transformable(newContext) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            Triple(m.execute(e1), m.execute(e2), m.execute(e3))
        }
    }

    override fun <A : Any> select(columnInfo: ColumnInfo<A>): ListQuery<A> {
        // TODO check
        val newContext = context.setColumn(columnInfo)
        return Transformable(newContext) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(columnInfo)
        }
    }

    override fun <A : Any, B : Any> select(c1: ColumnInfo<A>, c2: ColumnInfo<B>): ListQuery<Pair<A, B>> {
        // TODO check
        val newContext = context.setColumns(listOf(c1, c2))
        return Transformable(newContext) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(c1) to m.execute(c2)
        }
    }

    override fun <A : Any, B : Any, C : Any> select(
        c1: ColumnInfo<A>,
        c2: ColumnInfo<B>,
        c3: ColumnInfo<C>
    ): ListQuery<Triple<A, B, C>> {
        // TODO check
        val newContext = context.setColumns(listOf(c1, c2, c3))
        return Transformable(newContext) { dialect, rs: ResultSet ->
            val m = PropertyMapper(dialect, rs)
            Triple(m.execute(c1), m.execute(c2), m.execute(c3))
        }
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val terminal = createTerminal(context) { it.toList() }
        return terminal.run(config)
    }

    override fun toStatement(dialect: Dialect): Statement {
        val terminal = createTerminal(context) { it.toList() }
        return terminal.toStatement(dialect)
    }

    override fun peek(dialect: Dialect, block: (Statement) -> Unit): SqlSelectQueryImpl<ENTITY> {
        super.peek(dialect, block)
        return this
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
            mapper.execute(c.from)
        }
        return Terminal(c, provider, transformer)
    }
}

private class Transformable<T>(
    private val context: SqlSelectContext<*>,
    private val provider: (Dialect, ResultSet) -> T
) : ListQuery<T> {

    override fun run(config: DatabaseConfig): List<T> {
        val terminal = createTerminal { it.toList() }
        return terminal.run(config)
    }

    override fun toStatement(dialect: Dialect): Statement {
        val terminal = createTerminal { it.toList() }
        return terminal.toStatement(dialect)
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
        return Terminal(context, provider, transformer)
    }
}

private class Terminal<T, R>(
    private val context: SqlSelectContext<*>,
    private val provider: (Dialect, ResultSet) -> T,
    val transformer: (Sequence<T>) -> R
) : Query<R> {

    override fun run(config: DatabaseConfig): R {
        if (context.options.allowEmptyWhereClause == false && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = toStatement(config.dialect)
        val executor = JdbcExecutor(config, context.options)
        return executor.executeQuery(statement, provider, transformer)
    }

    override fun toStatement(dialect: Dialect): Statement {
        val builder = SqlSelectStatementBuilder(dialect, context)
        return builder.build()
    }
}
