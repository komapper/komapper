package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.command.PairColumnsSqlSelectCommand
import org.komapper.core.dsl.command.SingleColumnSqlSelectCommand
import org.komapper.core.dsl.command.SqlSelectCommand
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.scope.HavingDeclaration
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.JoinDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

interface SqlSelectQuery<ENTITY> : ListQuery<ENTITY> {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSelectQuery<ENTITY>
    fun groupBy(vararg items: ColumnInfo<*>): SqlSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): SqlSelectQuery<ENTITY>
    fun offset(value: Int): SqlSelectQuery<ENTITY>
    fun limit(value: Int): SqlSelectQuery<ENTITY>
    fun forUpdate(): SqlSelectQuery<ENTITY>

    fun <T : Any> select(columnInfo: ColumnInfo<T>): ListQuery<T>

    fun <A : Any, B : Any> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>
    ): ListQuery<Pair<A, B>>
}

internal data class SqlSelectQueryImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: SqlSelectContext<ENTITY> = SqlSelectContext(entityMetamodel)
) :
    SqlSelectQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, SqlSelectContext<ENTITY>> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, declaration)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.leftJoin(entityMetamodel, declaration)
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

    override fun <T : Any> select(columnInfo: ColumnInfo<T>): ListQuery<T> {
        val newContext = context.addColumn(columnInfo)
        return SingleColumnQuery(newContext, columnInfo, this::buildStatement)
    }

    override fun <A : Any, B : Any> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>
    ): ListQuery<Pair<A, B>> {
        val newContext = context.addColumns(listOf(columnInfo1, columnInfo2))
        return PairColumnsQuery(newContext, columnInfo1 to columnInfo2, this::buildStatement)
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val transformable = Transformable { it.toList() }
        return transformable.run(config)
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect, context)
    }

    override fun peek(dialect: Dialect, block: (Statement) -> Unit): SqlSelectQueryImpl<ENTITY> {
        super.peek(dialect, block)
        return this
    }

    private fun buildStatement(dialect: Dialect, c: SqlSelectContext<ENTITY>): Statement {
        val builder = SqlSelectStatementBuilder(dialect, c)
        return builder.build()
    }

    override fun first(): Query<ENTITY> {
        support.limit(1)
        return Transformable { it.first() }
    }

    override fun firstOrNull(): Query<ENTITY?> {
        support.limit(1)
        return Transformable { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<ENTITY>) -> R): Query<R> {
        return Transformable(transformer)
    }

    private inner class Transformable<R>(val transformer: (Sequence<ENTITY>) -> R) : Query<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = buildStatement(config.dialect, context)
            val command = SqlSelectCommand(entityMetamodel, config, statement, transformer)
            return command.execute()
        }

        override fun toStatement(dialect: Dialect): Statement = buildStatement(dialect, context)
    }
}

private class SingleColumnQuery<ENTITY, T : Any>(
    private val context: SqlSelectContext<ENTITY>,
    private val columnInfo: ColumnInfo<T>,
    private val statementBuilder: (Dialect, SqlSelectContext<ENTITY>) -> Statement
) : ListQuery<T> {

    override fun run(config: DatabaseConfig): List<T> {
        val transformable = Transformable { it.toList() }
        return transformable.run(config)
    }

    override fun toStatement(dialect: Dialect): Statement = statementBuilder(dialect, context)

    override fun first(): Query<T> {
        return Transformable { it.first() }
    }

    override fun firstOrNull(): Query<T?> {
        return Transformable { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<T>) -> R): Query<R> {
        return Transformable(transformer)
    }

    private inner class Transformable<R>(val transformer: (Sequence<T>) -> R) : Query<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = statementBuilder(config.dialect, context)
            val command = SingleColumnSqlSelectCommand(columnInfo, config, statement, transformer)
            return command.execute()
        }

        override fun toStatement(dialect: Dialect): Statement = statementBuilder(dialect, context)
    }
}

private class PairColumnsQuery<ENTITY, A : Any, B : Any>(
    private val context: SqlSelectContext<ENTITY>,
    private val pair: Pair<ColumnInfo<A>, ColumnInfo<B>>,
    private val statementBuilder: (Dialect, SqlSelectContext<ENTITY>) -> Statement
) : ListQuery<Pair<A, B>> {

    override fun run(config: DatabaseConfig): List<Pair<A, B>> {
        val transformable = Transformable { it.toList() }
        return transformable.run(config)
    }

    override fun toStatement(dialect: Dialect): Statement = statementBuilder(dialect, context)

    override fun first(): Query<Pair<A, B>> {
        return Transformable { it.first() }
    }

    override fun firstOrNull(): Query<Pair<A, B>?> {
        return Transformable { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<Pair<A, B>>) -> R): Query<R> {
        return Transformable(transformer)
    }

    private inner class Transformable<R>(val transformer: (Sequence<Pair<A, B>>) -> R) : Query<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = statementBuilder(config.dialect, context)
            val command = PairColumnsSqlSelectCommand(pair, config, statement, transformer)
            return command.execute()
        }

        override fun toStatement(dialect: Dialect): Statement = statementBuilder(dialect, context)
    }
}
