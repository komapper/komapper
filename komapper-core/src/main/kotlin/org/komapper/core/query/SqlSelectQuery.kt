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

interface SqlSelectQuery<ENTITY> : Query<List<ENTITY>> {

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
    fun orderBy(vararg items: ColumnInfo< *>): SqlSelectQuery<ENTITY>
    fun offset(value: Int): SqlSelectQuery<ENTITY>
    fun limit(value: Int): SqlSelectQuery<ENTITY>
    fun forUpdate(): SqlSelectQuery<ENTITY>
    fun <T : Any> select(columnInfo: ColumnInfo<T>): SqlSelectQuery1<ENTITY, T>
    fun <A : Any, B : Any> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>
    ): SqlSelectQuery2<ENTITY, A, B>
}

interface SqlSelectQuery1<ENTITY, T> : Query<List<T>>

interface SqlSelectQuery2<ENTITY, A, B> : Query<List<Pair<A, B>>>

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
    fun groupBy(vararg items: ColumnInfo<*>): SqlSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectSubQuery<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): SqlSelectSubQuery<ENTITY>
    fun offset(value: Int): SqlSelectSubQuery<ENTITY>
    fun limit(value: Int): SqlSelectSubQuery<ENTITY>
    fun select(columnInfo: ColumnInfo<*>): SingleColumnProjection
}

internal class SqlSelectQueryImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: SqlSelectContext<ENTITY> = SqlSelectContext(entityMetamodel)
) :
    SqlSelectQuery<ENTITY>,
    SqlSelectSubQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY> {
        support.innerJoin(entityMetamodel, declaration)
        return this
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY> {
        support.leftJoin(entityMetamodel, declaration)
        return this
    }

    override fun where(declaration: WhereDeclaration): SqlSelectQueryImpl<ENTITY> {
        support.where(declaration)
        return this
    }

    override fun groupBy(vararg items: ColumnInfo<*>): SqlSelectQueryImpl<ENTITY> {
        context.groupBy.addAll(items)
        return this
    }

    override fun having(declaration: HavingDeclaration): SqlSelectQueryImpl<ENTITY> {
        val support = FilterScopeSupport(context.having)
        val scope = HavingScope(support)
        declaration(scope)
        return this
    }

    override fun orderBy(vararg items: ColumnInfo<*>): SqlSelectQueryImpl<ENTITY> {
        support.orderBy(*items)
        return this
    }

    override fun offset(value: Int): SqlSelectQueryImpl<ENTITY> {
        support.offset(value)
        return this
    }

    override fun limit(value: Int): SqlSelectQueryImpl<ENTITY> {
        support.limit(value)
        return this
    }

    override fun forUpdate(): SqlSelectQueryImpl<ENTITY> {
        support.forUpdate()
        return this
    }

    override fun select(columnInfo: ColumnInfo<*>): SingleColumnProjection {
        context.columns.add(columnInfo)
        return SingleColumnProjection(context)
    }

    override fun <T : Any> select(columnInfo: ColumnInfo<T>): SingleProjectionQuery<ENTITY, T> {
        context.columns.add(columnInfo)
        return SingleProjectionQuery(context, columnInfo)
    }

    override fun <A : Any, B : Any> select(
        columnInfo1: ColumnInfo<A>,
        columnInfo2: ColumnInfo<B>
    ): PairProjectionQuery<ENTITY, A, B> {
        context.columns.add(columnInfo1)
        context.columns.add(columnInfo2)
        return PairProjectionQuery(context, columnInfo1 to columnInfo2)
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
}

internal class SingleProjectionQuery<ENTITY, T : Any>(
    private val context: SqlSelectContext<ENTITY>,
    private val columnInfo: ColumnInfo<T>
) : SqlSelectQuery1<ENTITY, T> {

    override fun run(config: DatabaseConfig): List<T> {
        val statement = buildStatement(config)
        val command = SingleColumnSqlSelectCommand(columnInfo, config, statement)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config, context)
        return builder.build()
    }
}

internal class PairProjectionQuery<ENTITY, A : Any, B : Any>(
    private val context: SqlSelectContext<ENTITY>,
    private val pair: Pair<ColumnInfo<A>, ColumnInfo<B>>
) : SqlSelectQuery2<ENTITY, A, B> {

    override fun run(config: DatabaseConfig): List<Pair<A, B>> {
        val statement = buildStatement(config)
        val command = PairColumnsSqlSelectCommand(pair, config, statement)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config, context)
        return builder.build()
    }
}
