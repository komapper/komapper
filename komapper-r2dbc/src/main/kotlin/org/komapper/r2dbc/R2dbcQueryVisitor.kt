package org.komapper.r2dbc

import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityDeleteBatchQuery
import org.komapper.core.dsl.query.EntityDeleteSingleQuery
import org.komapper.core.dsl.query.EntityInsertBatchQuery
import org.komapper.core.dsl.query.EntityInsertMultipleQuery
import org.komapper.core.dsl.query.EntityInsertSingleQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntityUpdateBatchQuery
import org.komapper.core.dsl.query.EntityUpdateSingleQuery
import org.komapper.core.dsl.query.EntityUpsertBatchQuery
import org.komapper.core.dsl.query.EntityUpsertMultipleQuery
import org.komapper.core.dsl.query.EntityUpsertSingleQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryRunner
import org.komapper.core.dsl.query.QueryVisitor
import org.komapper.core.dsl.query.SchemaCreateQueryImpl
import org.komapper.core.dsl.query.SchemaDropAllQueryImpl
import org.komapper.core.dsl.query.SchemaDropQueryImpl
import org.komapper.core.dsl.query.ScriptExecuteQueryImpl
import org.komapper.core.dsl.query.SqlDeleteQueryImpl
import org.komapper.core.dsl.query.SqlInsertQueryImpl
import org.komapper.core.dsl.query.SqlMultipleColumnsQuery
import org.komapper.core.dsl.query.SqlMultipleEntitiesQuery
import org.komapper.core.dsl.query.SqlPairColumnsQuery
import org.komapper.core.dsl.query.SqlPairColumnsSetOperationQuery
import org.komapper.core.dsl.query.SqlPairEntitiesQuery
import org.komapper.core.dsl.query.SqlPairEntitiesSetOperationQuery
import org.komapper.core.dsl.query.SqlSelectQueryImpl
import org.komapper.core.dsl.query.SqlSetOperationQueryImpl
import org.komapper.core.dsl.query.SqlSingleColumnQuery
import org.komapper.core.dsl.query.SqlSingleColumnSetOperationQuery
import org.komapper.core.dsl.query.SqlTripleColumnsQuery
import org.komapper.core.dsl.query.SqlTripleColumnsSetOperationQuery
import org.komapper.core.dsl.query.SqlTripleEntitiesQuery
import org.komapper.core.dsl.query.SqlUpdateQueryImpl
import org.komapper.core.dsl.query.TemplateExecuteQueryImpl
import org.komapper.core.dsl.query.TemplateSelectQueryImpl
import org.komapper.r2dbc.dsl.runner.EntityDeleteSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertMultipleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntitySelectQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityUpdateSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertMultipleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.Providers
import org.komapper.r2dbc.dsl.runner.R2dbcQueryRunner
import org.komapper.r2dbc.dsl.runner.SchemaCreateQueryRunner
import org.komapper.r2dbc.dsl.runner.SchemaDropAllQueryRunner
import org.komapper.r2dbc.dsl.runner.SchemaDropQueryRunner
import org.komapper.r2dbc.dsl.runner.ScriptExecuteQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlDeleteQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlInsertQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlMultipleColumnsQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlMultipleEntitiesQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlPairColumnsQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlPairEntitiesQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlSelectQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlSetOperationQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlSingleColumnQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlTripleColumnsQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlTripleEntitiesQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlUpdateQueryRunner
import org.komapper.r2dbc.dsl.runner.TemplateExecuteQueryRunner
import org.komapper.r2dbc.dsl.runner.TemplateSelectQueryRunner

class R2dbcQueryVisitor : QueryVisitor {

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> visit(query: Query.Plus<T, S>): QueryRunner {
        val left = query.left.accept(this) as R2dbcQueryRunner<T>
        val right = query.right.accept(this) as R2dbcQueryRunner<S>
        return R2dbcQueryRunner.Plus(left, right)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> visit(query: Query.FlatMap<T, S>): QueryRunner {
        val runner = query.query.accept(this) as R2dbcQueryRunner<T>
        val transform: (T) -> R2dbcQueryRunner<S> =
            { query.transform(it).accept(this@R2dbcQueryVisitor) as R2dbcQueryRunner<S> }
        return R2dbcQueryRunner.FlatMap(runner, transform)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> visit(query: Query.FlatZip<T, S>): QueryRunner {
        val runner = query.query.accept(this) as R2dbcQueryRunner<T>
        val transform: (T) -> R2dbcQueryRunner<S> =
            { query.transform(it).accept(this@R2dbcQueryVisitor) as R2dbcQueryRunner<S> }
        return R2dbcQueryRunner.FlatZip(runner, transform)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntitySelectQueryImpl<ENTITY, ID, META>): R2dbcQueryRunner<List<ENTITY>> {
        return EntitySelectQueryRunner(query.context, query.option) { it.toList() }
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    visit(query: EntitySelectQueryImpl.Collect<ENTITY, ID, META, R>): QueryRunner {
        return EntitySelectQueryRunner(query.context, query.option, query.transform)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityDeleteBatchQuery<ENTITY, ID, META>): QueryRunner {
        throw UnsupportedOperationException("Batch delete is not supported.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityDeleteSingleQuery<ENTITY, ID, META>): QueryRunner {
        return EntityDeleteSingleQueryRunner(query.context, query.entity, query.option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityInsertMultipleQuery<ENTITY, ID, META>): QueryRunner {
        return EntityInsertMultipleQueryRunner(query.context, query.entities, query.option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityInsertBatchQuery<ENTITY, ID, META>): QueryRunner {
        throw UnsupportedOperationException("Batch insert is not supported. Instead, use multiple insert.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityInsertSingleQuery<ENTITY, ID, META>): QueryRunner {
        return EntityInsertSingleQueryRunner(query.context, query.entity, query.option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpdateBatchQuery<ENTITY, ID, META>): QueryRunner {
        throw UnsupportedOperationException("Batch update is not supported. Instead, use multiple update.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpdateSingleQuery<ENTITY, ID, META>): QueryRunner {
        return EntityUpdateSingleQueryRunner(query.context, query.option, query.entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpsertBatchQuery<ENTITY, ID, META>): QueryRunner {
        throw UnsupportedOperationException("Batch upsert is not supported. Instead, use multiple upsert.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpsertMultipleQuery<ENTITY, ID, META>): QueryRunner {
        return EntityUpsertMultipleQueryRunner(query.context, query.option, query.entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpsertSingleQuery<ENTITY, ID, META>): QueryRunner {
        return EntityUpsertSingleQueryRunner(query.context, query.option, query.entity)
    }

    override fun visit(query: SchemaCreateQueryImpl): QueryRunner {
        return SchemaCreateQueryRunner(query.entityMetamodels, query.option)
    }

    override fun visit(query: SchemaDropQueryImpl): QueryRunner {
        return SchemaDropQueryRunner(query.entityMetamodels, query.option)
    }

    override fun visit(query: SchemaDropAllQueryImpl): QueryRunner {
        return SchemaDropAllQueryRunner(query.option)
    }

    override fun visit(query: ScriptExecuteQueryImpl): QueryRunner {
        return ScriptExecuteQueryRunner(query.sql, query.option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: SqlSelectQueryImpl<ENTITY, ID, META>): QueryRunner {
        val provide = Providers.singleEntity(query.context.target)
        return SqlSelectQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    visit(query: SqlSelectQueryImpl.Collect<ENTITY, ID, META, R>): QueryRunner {
        val provide = Providers.singleEntity(query.context.target)
        return SqlSelectQueryRunner(query.context, query.option, provide, query.transform)
    }

    override fun <T : Any> visit(query: SqlSetOperationQueryImpl<T>): QueryRunner {
        val provide = Providers.singleEntity(query.metamodel)
        return SqlSetOperationQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <T : Any, R> visit(query: SqlSetOperationQueryImpl.Collect<T, R>): QueryRunner {
        val provide = Providers.singleEntity(query.metamodel)
        return SqlSetOperationQueryRunner(query.context, query.option, provide, query.transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>
    visit(query: SqlPairEntitiesQuery<A, A_META, B, B_META>): QueryRunner {
        val provide = Providers.pairEntities(query.metamodels)
        return SqlPairEntitiesQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    visit(query: SqlPairEntitiesQuery.Collect<A, A_META, B, B_META, R>): QueryRunner {
        val provide = Providers.pairEntities(query.metamodels)
        return SqlPairEntitiesQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>> visit(
        query: SqlPairEntitiesSetOperationQuery<A, A_META, B, B_META>
    ): QueryRunner {
        val provide = Providers.pairEntities(query.metamodels)
        return SqlSetOperationQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R> visit(
        query: SqlPairEntitiesSetOperationQuery.Collect<A, A_META, B, B_META, R>
    ): QueryRunner {
        val provide = Providers.pairEntities(query.metamodels)
        return SqlSetOperationQueryRunner(query.context, query.option, provide, query.transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>> visit(
        query: SqlTripleEntitiesQuery<A, A_META, B, B_META, C, C_META>
    ): QueryRunner {
        val provide = Providers.tripleEntities(query.metamodels)
        return SqlTripleEntitiesQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun visit(query: SqlMultipleEntitiesQuery): QueryRunner {
        val provide = Providers.multipleEntities(query.metamodels)
        return SqlMultipleEntitiesQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any> visit(query: SqlSingleColumnQuery<A>): QueryRunner {
        val provide = Providers.singleColumn(query.expression)
        return SqlSingleColumnQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any, R> visit(query: SqlSingleColumnQuery.Collect<A, R>): QueryRunner {
        val provide = Providers.singleColumn(query.expression)
        return SqlSingleColumnQueryRunner(query.context, query.option, provide, query.transform)
    }

    override fun <A : Any> visit(query: SqlSingleColumnSetOperationQuery<A>): QueryRunner {
        val provide = Providers.singleColumn(query.expression)
        return SqlSetOperationQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any, R> visit(query: SqlSingleColumnSetOperationQuery.Collect<A, R>): QueryRunner {
        val provide = Providers.singleColumn(query.expression)
        return SqlSetOperationQueryRunner(query.context, query.option, provide, query.transform)
    }

    override fun <A : Any, B : Any> visit(query: SqlPairColumnsQuery<A, B>): QueryRunner {
        val provide = Providers.pairColumns(query.expressions)
        return SqlPairColumnsQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any, B : Any, R> visit(query: SqlPairColumnsQuery.Collect<A, B, R>): QueryRunner {
        val provide = Providers.pairColumns(query.expressions)
        return SqlPairColumnsQueryRunner(query.context, query.option, provide, query.transform)
    }

    override fun <A : Any, B : Any> visit(query: SqlPairColumnsSetOperationQuery<A, B>): QueryRunner {
        val provide = Providers.pairColumns(query.expressions)
        return SqlSetOperationQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any, B : Any, R> visit(query: SqlPairColumnsSetOperationQuery.Collect<A, B, R>): QueryRunner {
        val provide = Providers.pairColumns(query.expressions)
        return SqlSetOperationQueryRunner(query.context, query.option, provide, query.transform)
    }

    override fun <A : Any, B : Any, C : Any> visit(query: SqlTripleColumnsQuery<A, B, C>): QueryRunner {
        val provide = Providers.tripleColumns(query.expressions)
        return SqlTripleColumnsQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any, B : Any, C : Any, R> visit(query: SqlTripleColumnsQuery.Collect<A, B, C, R>): QueryRunner {
        val provide = Providers.tripleColumns(query.expressions)
        return SqlTripleColumnsQueryRunner(query.context, query.option, provide, query.transform)
    }

    override fun <A : Any, B : Any, C : Any> visit(query: SqlTripleColumnsSetOperationQuery<A, B, C>): QueryRunner {
        val provide = Providers.tripleColumns(query.expressions)
        return SqlSetOperationQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <A : Any, B : Any, C : Any, R> visit(query: SqlTripleColumnsSetOperationQuery.Collect<A, B, C, R>): QueryRunner {
        val provide = Providers.tripleColumns(query.expressions)
        return SqlSetOperationQueryRunner(query.context, query.option, provide, query.transform)
    }

    override fun visit(query: SqlMultipleColumnsQuery): QueryRunner {
        val provide = Providers.multipleColumns(query.expressions)
        return SqlMultipleColumnsQueryRunner(query.context, query.option, provide) { it.toList() }
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: SqlDeleteQueryImpl<ENTITY, ID, META>): QueryRunner {
        return SqlDeleteQueryRunner(query.context, query.option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: SqlInsertQueryImpl<ENTITY, ID, META>): QueryRunner {
        return SqlInsertQueryRunner(query.context, query.option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: SqlUpdateQueryImpl<ENTITY, ID, META>): QueryRunner {
        return SqlUpdateQueryRunner(query.context, query.option)
    }

    override fun visit(query: TemplateExecuteQueryImpl): QueryRunner {
        return TemplateExecuteQueryRunner(query.sql, query.params, query.option)
    }

    override fun <T> visit(query: TemplateSelectQueryImpl<T>): QueryRunner {
        return TemplateSelectQueryRunner(query.sql, query.params, query.provide, query.option) { it.toList() }
    }

    override fun <T, R> visit(query: TemplateSelectQueryImpl.Collect<T, R>): QueryRunner {
        return TemplateSelectQueryRunner(query.sql, query.params, query.provide, query.option, query.transform)
    }
}
