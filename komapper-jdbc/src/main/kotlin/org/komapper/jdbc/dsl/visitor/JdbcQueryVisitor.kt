package org.komapper.jdbc.dsl.visitor

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteBatchOptions
import org.komapper.core.dsl.options.EntityDeleteOptions
import org.komapper.core.dsl.options.EntityInsertBatchOptions
import org.komapper.core.dsl.options.EntityInsertOptions
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.core.dsl.options.EntityUpdateBatchOptions
import org.komapper.core.dsl.options.EntityUpdateOptions
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.options.SchemaCreateOptions
import org.komapper.core.dsl.options.SchemaDropAllOptions
import org.komapper.core.dsl.options.SchemaDropOptions
import org.komapper.core.dsl.options.ScriptExecuteOptions
import org.komapper.core.dsl.options.SqlDeleteOptions
import org.komapper.core.dsl.options.SqlInsertOptions
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.options.SqlUpdateOptions
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Entities
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.jdbc.dsl.query.MetadataQueryImpl
import org.komapper.jdbc.dsl.runner.EntityDeleteBatchQueryRunner
import org.komapper.jdbc.dsl.runner.EntityDeleteSingleQueryRunner
import org.komapper.jdbc.dsl.runner.EntityInsertBatchQueryRunner
import org.komapper.jdbc.dsl.runner.EntityInsertMultipleQueryRunner
import org.komapper.jdbc.dsl.runner.EntityInsertSingleQueryRunner
import org.komapper.jdbc.dsl.runner.EntitySelectQueryRunner
import org.komapper.jdbc.dsl.runner.EntityUpdateBatchQueryRunner
import org.komapper.jdbc.dsl.runner.EntityUpdateSingleQueryRunner
import org.komapper.jdbc.dsl.runner.EntityUpsertBatchQueryRunner
import org.komapper.jdbc.dsl.runner.EntityUpsertMultipleQueryRunner
import org.komapper.jdbc.dsl.runner.EntityUpsertSingleQueryRunner
import org.komapper.jdbc.dsl.runner.JdbcQueryRunner
import org.komapper.jdbc.dsl.runner.MetadataQueryRunner
import org.komapper.jdbc.dsl.runner.ResultSetTransformers
import org.komapper.jdbc.dsl.runner.SchemaCreateQueryRunner
import org.komapper.jdbc.dsl.runner.SchemaDropAllQueryRunner
import org.komapper.jdbc.dsl.runner.SchemaDropQueryRunner
import org.komapper.jdbc.dsl.runner.ScriptExecuteQueryRunner
import org.komapper.jdbc.dsl.runner.SqlDeleteQueryRunner
import org.komapper.jdbc.dsl.runner.SqlInsertQueryRunner
import org.komapper.jdbc.dsl.runner.SqlSelectQueryRunner
import org.komapper.jdbc.dsl.runner.SqlSetOperationQueryRunner
import org.komapper.jdbc.dsl.runner.SqlUpdateQueryRunner
import org.komapper.jdbc.dsl.runner.TemplateExecuteQueryRunner
import org.komapper.jdbc.dsl.runner.TemplateSelectQueryRunner

internal class JdbcQueryVisitor : QueryVisitor {

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> plusQuery(left: Query<T>, right: Query<S>): QueryRunner {
        val leftRunner = left.accept(this) as JdbcQueryRunner<T>
        val rightRunner = right.accept(this) as JdbcQueryRunner<S>
        return JdbcQueryRunner.Plus(leftRunner, rightRunner)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): QueryRunner {
        val runner = query.accept(this) as JdbcQueryRunner<T>
        return JdbcQueryRunner.FlatMap(runner) {
            transform(it).accept(this@JdbcQueryVisitor) as JdbcQueryRunner<S>
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): QueryRunner {
        val runner = query.accept(this) as JdbcQueryRunner<T>
        return JdbcQueryRunner.FlatZip(runner) {
            transform(it).accept(this@JdbcQueryVisitor) as JdbcQueryRunner<S>
        }
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    entitySelectQuery(
        context: EntitySelectContext<ENTITY, ID, META>,
        options: EntitySelectOptions,
        transform: suspend (Flow<ENTITY>) -> R
    ): QueryRunner {
        return EntitySelectQueryRunner(context, options, transform)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteBatchOptions,
        entities: List<ENTITY>
    ): QueryRunner {
        return EntityDeleteBatchQueryRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteOptions,
        entity: ENTITY
    ): QueryRunner {
        return EntityDeleteSingleQueryRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entities: List<ENTITY>
    ): QueryRunner {
        return EntityInsertMultipleQueryRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertBatchOptions,
        entities: List<ENTITY>
    ): QueryRunner {
        return EntityInsertBatchQueryRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entity: ENTITY
    ): QueryRunner {
        return EntityInsertSingleQueryRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateBatchOptions,
        entities: List<ENTITY>
    ): QueryRunner {
        return EntityUpdateBatchQueryRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateOptions,
        entity: ENTITY
    ): QueryRunner {
        return EntityUpdateSingleQueryRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): QueryRunner {
        return EntityUpsertBatchQueryRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): QueryRunner {
        return EntityUpsertMultipleQueryRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY,
    ): QueryRunner {
        return EntityUpsertSingleQueryRunner(context, options, entity)
    }

    override fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaCreateOptions
    ): QueryRunner {
        return SchemaCreateQueryRunner(entityMetamodels, options)
    }

    override fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaDropOptions
    ): QueryRunner {
        return SchemaDropQueryRunner(entityMetamodels, options)
    }

    override fun schemaDropAllQuery(options: SchemaDropAllOptions): QueryRunner {
        return SchemaDropAllQueryRunner(options)
    }

    override fun scriptExecuteQuery(
        sql: String,
        options: ScriptExecuteOptions
    ): QueryRunner {
        return ScriptExecuteQueryRunner(sql, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        options: SqlSelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.singleEntity(context.target)
        return SqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <T : Any, R> sqlSetOperationQuery(
        context: SqlSetOperationContext<T>,
        options: SqlSetOperationOptions,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.singleEntity(metamodel)
        return SqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    sqlPairEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Pair<A_META, B_META>,
        collect: suspend (Flow<Pair<A, B?>>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.pairEntities(metamodels)
        return SqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    sqlPairEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Pair<A, B?>>,
        options: SqlSetOperationOptions,
        metamodels: Pair<A_META, B_META>,
        collect: suspend (Flow<Pair<A, B?>>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.pairEntities(metamodels)
        return SqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>, R>
    sqlTripleEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Triple<A_META, B_META, C_META>,
        collect: suspend (Flow<Triple<A, B?, C?>>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.tripleEntities(metamodels)
        return SqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>, R> sqlTripleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Triple<A, B?, C?>>,
        options: SqlSetOperationOptions,
        metamodels: Triple<A_META, B_META, C_META>,
        collect: suspend (Flow<Triple<A, B?, C?>>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.tripleEntities(metamodels)
        return SqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleEntitiesQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        metamodels: List<EntityMetamodel<*, *, *>>,
        collect: suspend (Flow<Entities>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.multipleEntities(metamodels)
        return SqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Entities>,
        options: SqlSetOperationOptions,
        metamodels: List<EntityMetamodel<*, *, *>>,
        collect: suspend (Flow<Entities>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.multipleEntities(metamodels)
        return SqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, R> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.singleColumn(expression)
        return SqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, R> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext<A?>,
        options: SqlSetOperationOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.singleColumn(expression)
        return SqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.pairColumns(expressions)
        return SqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext<Pair<A?, B?>>,
        options: SqlSetOperationOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.pairColumns(expressions)
        return SqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.tripleColumns(expressions)
        return SqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Triple<A?, B?, C?>>,
        options: SqlSetOperationOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.tripleColumns(expressions)
        return SqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.multipleColumns(expressions)
        return SqlSelectQueryRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Columns>,
        options: SqlSetOperationOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): QueryRunner {
        val transform = ResultSetTransformers.multipleColumns(expressions)
        return SqlSetOperationQueryRunner(context, options, transform, collect)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlDeleteQuery(
        context: SqlDeleteContext<ENTITY, ID, META>,
        options: SqlDeleteOptions
    ): QueryRunner {
        return SqlDeleteQueryRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlInsertQuery(
        context: SqlInsertContext<ENTITY, ID, META>,
        options: SqlInsertOptions
    ): QueryRunner {
        return SqlInsertQueryRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlUpdateQuery(
        context: SqlUpdateContext<ENTITY, ID, META>,
        options: SqlUpdateOptions
    ): QueryRunner {
        return SqlUpdateQueryRunner(context, options)
    }

    override fun templateExecuteQuery(
        sql: String,
        params: Any,
        options: TemplateExecuteOptions
    ): QueryRunner {
        return TemplateExecuteQueryRunner(sql, params, options)
    }

    override fun <T, R> templateSelectQuery(
        sql: String,
        params: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions,
        collect: suspend (Flow<T>) -> R
    ): QueryRunner {
        return TemplateSelectQueryRunner(sql, params, transform, options, collect)
    }

    fun visit(query: MetadataQueryImpl): QueryRunner {
        return MetadataQueryRunner(query.catalog, query.schemaName, query.tableNamePattern, query.tableTypes)
    }
}
