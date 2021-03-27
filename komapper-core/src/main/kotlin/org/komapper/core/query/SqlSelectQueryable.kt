package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.SqlSelectStatementBuilder
import org.komapper.core.query.command.PairColumnsSqlSelectCommand
import org.komapper.core.query.command.SingleColumnSqlSelectCommand
import org.komapper.core.query.command.SqlSelectCommand
import org.komapper.core.query.context.SqlSelectContext
import org.komapper.core.query.scope.HavingDeclaration
import org.komapper.core.query.scope.HavingScope
import org.komapper.core.query.scope.JoinDeclaration
import org.komapper.core.query.scope.WhereDeclaration

interface SqlSelectQueryable<ENTITY> : ListQueryable<ENTITY> {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryable<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryable<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSelectQueryable<ENTITY>
    fun groupBy(vararg items: ColumnInfo<*>): SqlSelectQueryable<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectQueryable<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): SqlSelectQueryable<ENTITY>
    fun offset(value: Int): SqlSelectQueryable<ENTITY>
    fun limit(value: Int): SqlSelectQueryable<ENTITY>
    fun forUpdate(): SqlSelectQueryable<ENTITY>

    fun <T : Any> select(columnInfo: ColumnInfo<T>): ListQueryable<T>

    fun <A : Any, B : Any> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>
    ): ListQueryable<Pair<A, B>>
}

interface SqlSelectSubQuery<ENTITY> {
    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectSubQuery<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectSubQuery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSelectSubQuery<ENTITY>
    fun groupBy(vararg items: ColumnInfo<*>): SqlSelectQueryable<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectSubQuery<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): SqlSelectSubQuery<ENTITY>
    fun offset(value: Int): SqlSelectSubQuery<ENTITY>
    fun limit(value: Int): SqlSelectSubQuery<ENTITY>
    fun select(columnInfo: ColumnInfo<*>): Projection
}

internal data class SqlSelectQueryableImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: SqlSelectContext<ENTITY> = SqlSelectContext(entityMetamodel)
) :
    SqlSelectQueryable<ENTITY>,
    SqlSelectSubQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, SqlSelectContext<ENTITY>> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryableImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, declaration)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryableImpl<ENTITY> {
        val newContext = support.leftJoin(entityMetamodel, declaration)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlSelectQueryableImpl<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun groupBy(vararg items: ColumnInfo<*>): SqlSelectQueryableImpl<ENTITY> {
        val newContext = context.copy(groupBy = items.toList())
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): SqlSelectQueryableImpl<ENTITY> {
        val scope = HavingScope()
        declaration(scope)
        val newContext = context.addHaving(scope.criteria.toList())
        return copy(context = newContext)
    }

    override fun orderBy(vararg items: ColumnInfo<*>): SqlSelectQueryableImpl<ENTITY> {
        val newContext = support.orderBy(*items)
        return copy(context = newContext)
    }

    override fun offset(value: Int): SqlSelectQueryableImpl<ENTITY> {
        val newContext = support.offset(value)
        return copy(context = newContext)
    }

    override fun limit(value: Int): SqlSelectQueryableImpl<ENTITY> {
        val newContext = support.limit(value)
        return copy(context = newContext)
    }

    override fun forUpdate(): SqlSelectQueryableImpl<ENTITY> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun select(columnInfo: ColumnInfo<*>): Projection {
        val newContext = context.addColumn(columnInfo)
        return Projection.SingleColumn(newContext)
    }

    override fun <T : Any> select(columnInfo: ColumnInfo<T>): ListQueryable<T> {
        val newContext = context.addColumn(columnInfo)
        return SingleColumnQueryable(newContext, columnInfo, this::buildStatement)
    }

    override fun <A : Any, B : Any> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>
    ): ListQueryable<Pair<A, B>> {
        // TODO
        val newContext = context.addColumn(columnInfo1).addColumn(columnInfo2)
        return PairColumnsQueryable(newContext, columnInfo1 to columnInfo2, this::buildStatement)
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val transformable = Transformable { it.toList() }
        return transformable.run(config)
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config, context)
    }

    private fun buildStatement(config: DatabaseConfig, c: SqlSelectContext<ENTITY>): Statement {
        val builder = SqlSelectStatementBuilder(config, c)
        return builder.build()
    }

    override fun first(): Queryable<ENTITY> {
        support.limit(1)
        return Transformable { it.first() }
    }

    override fun firstOrNull(): Queryable<ENTITY?> {
        support.limit(1)
        return Transformable { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<ENTITY>) -> R): Queryable<R> {
        return Transformable(transformer)
    }

    private inner class Transformable<R>(val transformer: (Sequence<ENTITY>) -> R) : Queryable<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = buildStatement(config, context)
            val command = SqlSelectCommand(entityMetamodel, config, statement, transformer)
            return command.execute()
        }

        override fun toStatement(config: DatabaseConfig): Statement = buildStatement(config, context)
    }
}

private class SingleColumnQueryable<ENTITY, T : Any>(
    private val context: SqlSelectContext<ENTITY>,
    private val columnInfo: ColumnInfo<T>,
    private val statementBuilder: (DatabaseConfig, SqlSelectContext<ENTITY>) -> Statement
) : ListQueryable<T> {

    override fun run(config: DatabaseConfig): List<T> {
        val transformable = Transformable { it.toList() }
        return transformable.run(config)
    }

    override fun toStatement(config: DatabaseConfig): Statement = statementBuilder(config, context)

    override fun first(): Queryable<T> {
        return Transformable { it.first() }
    }

    override fun firstOrNull(): Queryable<T?> {
        return Transformable { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<T>) -> R): Queryable<R> {
        return Transformable(transformer)
    }

    private inner class Transformable<R>(val transformer: (Sequence<T>) -> R) : Queryable<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = statementBuilder(config, context)
            val command = SingleColumnSqlSelectCommand(columnInfo, config, statement, transformer)
            return command.execute()
        }

        override fun toStatement(config: DatabaseConfig): Statement = statementBuilder(config, context)
    }
}

private class PairColumnsQueryable<ENTITY, A : Any, B : Any>(
    private val context: SqlSelectContext<ENTITY>,
    private val pair: Pair<ColumnInfo<A>, ColumnInfo<B>>,
    private val statementBuilder: (DatabaseConfig, SqlSelectContext<ENTITY>) -> Statement
) : ListQueryable<Pair<A, B>> {

    override fun run(config: DatabaseConfig): List<Pair<A, B>> {
        val transformable = Transformable { it.toList() }
        return transformable.run(config)
    }

    override fun toStatement(config: DatabaseConfig): Statement = statementBuilder(config, context)

    override fun first(): Queryable<Pair<A, B>> {
        return Transformable { it.first() }
    }

    override fun firstOrNull(): Queryable<Pair<A, B>?> {
        return Transformable { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<Pair<A, B>>) -> R): Queryable<R> {
        return Transformable(transformer)
    }

    private inner class Transformable<R>(val transformer: (Sequence<Pair<A, B>>) -> R) : Queryable<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = statementBuilder(config, context)
            val command = PairColumnsSqlSelectCommand(pair, config, statement, transformer)
            return command.execute()
        }

        override fun toStatement(config: DatabaseConfig): Statement = statementBuilder(config, context)
    }
}
