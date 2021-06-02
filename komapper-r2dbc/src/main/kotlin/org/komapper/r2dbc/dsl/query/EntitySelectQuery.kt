package org.komapper.r2dbc.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntitySelectStatementBuilder
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntitySelectOption
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

interface EntitySelectQuery<ENTITY : Any> : Subquery<ENTITY> {
    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun first(declaration: WhereDeclaration): Query<ENTITY>
    fun firstOrNull(declaration: WhereDeclaration): Query<ENTITY?>
    fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY>
    fun orderBy(vararg expressions: ColumnExpression<*, *>): EntitySelectQuery<ENTITY>
    fun offset(offset: Int): EntitySelectQuery<ENTITY>
    fun limit(limit: Int): EntitySelectQuery<ENTITY>
    fun forUpdate(): EntitySelectQuery<ENTITY>
    fun option(configure: (EntitySelectOption) -> EntitySelectOption): EntitySelectQuery<ENTITY>
    fun asSqlQuery(): SqlSelectQuery<ENTITY>
}

internal data class EntitySelectQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val option: EntitySelectOption = EntitySelectOption.default
) :
    EntitySelectQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, ID, META, EntitySelectContext<ENTITY, ID, META>> =
        SelectQuerySupport(context)

    override val subqueryContext = SubqueryContext.EntitySelect<ENTITY>(context)

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.innerJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY, ID, META> {
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

    override fun where(declaration: WhereDeclaration): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.orderBy(*expressions)
        return copy(context = newContext)
    }

    override fun offset(offset: Int): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.offset(offset)
        return copy(context = newContext)
    }

    override fun limit(limit: Int): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.limit(limit)
        return copy(context = newContext)
    }

    override fun forUpdate(): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun option(configure: (EntitySelectOption) -> EntitySelectOption): EntitySelectQuery<ENTITY> {
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

    override fun asSqlQuery(): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(context.asSqlSelectContext(), option.asSqlSelectOption())
    }

    override suspend fun run(config: R2dbcDatabaseConfig): Flow<ENTITY> {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        return executor.executeQuery(statement) { row, _ ->
            val mapper = EntityMapper(config.dialect, row)
            mapper.execute(context.target) as ENTITY
        }.distinctUntilChangedBy { context.target.getId(it) }
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = EntitySelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
