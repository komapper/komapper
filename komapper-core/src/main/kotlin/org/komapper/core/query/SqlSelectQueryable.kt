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
import org.komapper.core.query.scope.FilterScopeSupport
import org.komapper.core.query.scope.HavingDeclaration
import org.komapper.core.query.scope.HavingScope
import org.komapper.core.query.scope.JoinDeclaration
import org.komapper.core.query.scope.WhereDeclaration

interface SqlSelectQueryable<ENTITY> : Queryable<List<ENTITY>> {

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
    fun <T : Any> select(columnInfo: ColumnInfo<T>): Queryable<List<T>>
    fun <T : Any, R> select(columnInfo: ColumnInfo<T>, transformer: (Sequence<T>) -> R): Queryable<R>

    fun <A : Any, B : Any> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>
    ): Queryable<List<Pair<A, B>>>

    fun <A : Any, B : Any, R> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>,
        transformer: (Sequence<Pair<A, B>>) -> R
    ): Queryable<R>

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
    fun select(columnInfo: ColumnInfo<*>): SingleColumnProjection
}

internal class SqlSelectQueryableImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: SqlSelectContext<ENTITY> = SqlSelectContext(entityMetamodel)
) :
    SqlSelectQueryable<ENTITY>,
    SqlSelectSubQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryableImpl<ENTITY> {
        support.innerJoin(entityMetamodel, declaration)
        return this
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryableImpl<ENTITY> {
        support.leftJoin(entityMetamodel, declaration)
        return this
    }

    override fun where(declaration: WhereDeclaration): SqlSelectQueryableImpl<ENTITY> {
        support.where(declaration)
        return this
    }

    override fun groupBy(vararg items: ColumnInfo<*>): SqlSelectQueryableImpl<ENTITY> {
        context.groupBy.addAll(items)
        return this
    }

    override fun having(declaration: HavingDeclaration): SqlSelectQueryableImpl<ENTITY> {
        val support = FilterScopeSupport(context.having)
        val scope = HavingScope(support)
        declaration(scope)
        return this
    }

    override fun orderBy(vararg items: ColumnInfo<*>): SqlSelectQueryableImpl<ENTITY> {
        support.orderBy(*items)
        return this
    }

    override fun offset(value: Int): SqlSelectQueryableImpl<ENTITY> {
        support.offset(value)
        return this
    }

    override fun limit(value: Int): SqlSelectQueryableImpl<ENTITY> {
        support.limit(value)
        return this
    }

    override fun forUpdate(): SqlSelectQueryableImpl<ENTITY> {
        support.forUpdate()
        return this
    }

    override fun select(columnInfo: ColumnInfo<*>): SingleColumnProjection {
        context.columns.add(columnInfo)
        return SingleColumnProjection(context)
    }

    override fun <T : Any> select(columnInfo: ColumnInfo<T>): Queryable<List<T>> {
        context.columns.add(columnInfo)
        return SingleColumnQueryable(columnInfo) { it.toList() }
    }

    override fun <T : Any, R> select(columnInfo: ColumnInfo<T>, transformer: (Sequence<T>) -> R): Queryable<R> {
        context.columns.add(columnInfo)
        return SingleColumnQueryable(columnInfo, transformer)
    }

    override fun <A : Any, B : Any> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>
    ): Queryable<List<Pair<A, B>>> {
        context.columns.add(columnInfo1)
        context.columns.add(columnInfo2)
        return PairColumnsQueryable(columnInfo1 to columnInfo2) { it.toList() }
    }

    override fun <A : Any, B : Any, R> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>,
        transformer: (Sequence<Pair<A, B>>) -> R
    ): Queryable<R> {
        context.columns.add(columnInfo1)
        context.columns.add(columnInfo2)
        return PairColumnsQueryable(columnInfo1 to columnInfo2, transformer)
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val statement = buildStatement(config)
        val command = SqlSelectCommand(entityMetamodel, config, statement)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config, context)
        return builder.build()
    }

    inner class SingleColumnQueryable<T : Any, R>(
        private val columnInfo: ColumnInfo<T>,
        private val transformer: (Sequence<T>) -> R
    ) : Queryable<R> {

        override fun run(config: DatabaseConfig): R {
            val statement = buildStatement(config)
            val command = SingleColumnSqlSelectCommand(columnInfo, config, statement, transformer)
            return command.execute()
        }

        override fun toStatement(config: DatabaseConfig): Statement = buildStatement(config)
    }

    inner class PairColumnsQueryable<A : Any, B : Any, R>(
        private val pair: Pair<ColumnInfo<A>, ColumnInfo<B>>,
        private val transformer: (Sequence<Pair<A, B>>) -> R
    ) : Queryable<R> {

        override fun run(config: DatabaseConfig): R {
            val statement = buildStatement(config)
            val command = PairColumnsSqlSelectCommand(pair, config, statement, transformer)
            return command.execute()
        }

        override fun toStatement(config: DatabaseConfig): Statement = buildStatement(config)
    }
}
