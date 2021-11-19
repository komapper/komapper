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
import org.komapper.core.dsl.options.EntityUpdateBatchOptions
import org.komapper.core.dsl.options.EntityUpdateOptions
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.options.SchemaCreateOptions
import org.komapper.core.dsl.options.SchemaDropAllOptions
import org.komapper.core.dsl.options.SchemaDropOptions
import org.komapper.core.dsl.options.ScriptExecuteOptions
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.options.SqlDeleteOptions
import org.komapper.core.dsl.options.SqlInsertOptions
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.options.SqlUpdateOptions
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.jdbc.dsl.runner.EntityContextJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityDeleteBatchJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityDeleteSingleJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityInsertBatchJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityInsertMultipleJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityInsertSingleJdbcRunner
import org.komapper.jdbc.dsl.runner.EntitySelectJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityUpdateBatchJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityUpdateSingleJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityUpsertBatchJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityUpsertMultipleJdbcRunner
import org.komapper.jdbc.dsl.runner.EntityUpsertSingleJdbcRunner
import org.komapper.jdbc.dsl.runner.JdbcResultSetTransformers
import org.komapper.jdbc.dsl.runner.JdbcRunner
import org.komapper.jdbc.dsl.runner.SchemaCreateJdbcRunner
import org.komapper.jdbc.dsl.runner.SchemaDropAllJdbcRunner
import org.komapper.jdbc.dsl.runner.SchemaDropJdbcRunner
import org.komapper.jdbc.dsl.runner.ScriptExecuteJdbcRunner
import org.komapper.jdbc.dsl.runner.SqlDeleteJdbcRunner
import org.komapper.jdbc.dsl.runner.SqlInsertJdbcRunner
import org.komapper.jdbc.dsl.runner.SqlSelectJdbcRunner
import org.komapper.jdbc.dsl.runner.SqlSetOperationJdbcRunner
import org.komapper.jdbc.dsl.runner.SqlUpdateJdbcRunner
import org.komapper.jdbc.dsl.runner.TemplateExecuteJdbcRunner
import org.komapper.jdbc.dsl.runner.TemplateSelectJdbcRunner

internal object JdbcQueryVisitor : QueryVisitor<JdbcRunner<*>> {

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> andThenQuery(left: Query<T>, right: Query<S>): JdbcRunner<S> {
        val leftRunner = left.accept(this) as JdbcRunner<T>
        val rightRunner = right.accept(this) as JdbcRunner<S>
        return JdbcRunner.AndThen(leftRunner, rightRunner)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> mapQuery(query: Query<T>, transform: (T) -> S): JdbcRunner<S> {
        val runner = query.accept(this) as JdbcRunner<T>
        return JdbcRunner.Map(runner, transform)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> zipQuery(left: Query<T>, right: Query<S>): JdbcRunner<Pair<T, S>> {
        val leftRunner = left.accept(this) as JdbcRunner<T>
        val rightRunner = right.accept(this) as JdbcRunner<S>
        return JdbcRunner.Zip(leftRunner, rightRunner)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): JdbcRunner<S> {
        val runner = query.accept(this) as JdbcRunner<T>
        return JdbcRunner.FlatMap(runner) {
            transform(it).accept(this@JdbcQueryVisitor) as JdbcRunner<S>
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): JdbcRunner<Pair<T, S>> {
        val runner = query.accept(this) as JdbcRunner<T>
        return JdbcRunner.FlatZip(runner) {
            transform(it).accept(this@JdbcQueryVisitor) as JdbcRunner<S>
        }
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityContextQuery(
        context: EntitySelectContext<ENTITY, ID, META>,
        options: SelectOptions
    ): JdbcRunner<*> {
        return EntityContextJdbcRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    entitySelectQuery(
        context: EntitySelectContext<ENTITY, ID, META>,
        options: SelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): JdbcRunner<R> {
        val transformer = JdbcResultSetTransformers.singleEntity(context.target)
        return EntitySelectJdbcRunner(context, options, transformer, collect)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteBatchOptions,
        entities: List<ENTITY>
    ): JdbcRunner<Unit> {
        return EntityDeleteBatchJdbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteOptions,
        entity: ENTITY
    ): JdbcRunner<Unit> {
        return EntityDeleteSingleJdbcRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entities: List<ENTITY>
    ): JdbcRunner<List<ENTITY>> {
        return EntityInsertMultipleJdbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertBatchOptions,
        entities: List<ENTITY>
    ): JdbcRunner<List<ENTITY>> {
        return EntityInsertBatchJdbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entity: ENTITY
    ): JdbcRunner<ENTITY> {
        return EntityInsertSingleJdbcRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateBatchOptions,
        entities: List<ENTITY>
    ): JdbcRunner<List<ENTITY>> {
        return EntityUpdateBatchJdbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateOptions,
        entity: ENTITY
    ): JdbcRunner<ENTITY> {
        return EntityUpdateSingleJdbcRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): JdbcRunner<List<Int>> {
        return EntityUpsertBatchJdbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): JdbcRunner<Int> {
        return EntityUpsertMultipleJdbcRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY,
    ): JdbcRunner<Int> {
        return EntityUpsertSingleJdbcRunner(context, options, entity)
    }

    override fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaCreateOptions
    ): JdbcRunner<Unit> {
        return SchemaCreateJdbcRunner(entityMetamodels, options)
    }

    override fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaDropOptions
    ): JdbcRunner<Unit> {
        return SchemaDropJdbcRunner(entityMetamodels, options)
    }

    override fun schemaDropAllQuery(options: SchemaDropAllOptions): JdbcRunner<Unit> {
        return SchemaDropAllJdbcRunner(options)
    }

    override fun scriptExecuteQuery(
        sql: String,
        options: ScriptExecuteOptions
    ): JdbcRunner<Unit> {
        return ScriptExecuteJdbcRunner(sql, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        options: SelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleEntity(context.target)
        return SqlSelectJdbcRunner(context, options, transform, collect)
    }

    override fun <T : Any, R> sqlSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleEntity(metamodel)
        return SqlSetOperationJdbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, R> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        options: SelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return SqlSelectJdbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, R> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.singleColumn(expression)
        return SqlSetOperationJdbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return SqlSelectJdbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.pairColumns(expressions)
        return SqlSetOperationJdbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return SqlSelectJdbcRunner(context, options, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.tripleColumns(expressions)
        return SqlSetOperationJdbcRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.multipleColumns(expressions)
        return SqlSelectJdbcRunner(context, options, transform, collect)
    }

    override fun <R> sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): JdbcRunner<R> {
        val transform = JdbcResultSetTransformers.multipleColumns(expressions)
        return SqlSetOperationJdbcRunner(context, options, transform, collect)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlDeleteQuery(
        context: SqlDeleteContext<ENTITY, ID, META>,
        options: SqlDeleteOptions
    ): JdbcRunner<Int> {
        return SqlDeleteJdbcRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlInsertQuery(
        context: SqlInsertContext<ENTITY, ID, META>,
        options: SqlInsertOptions
    ): JdbcRunner<Pair<Int, ID?>> {
        return SqlInsertJdbcRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlUpdateQuery(
        context: SqlUpdateContext<ENTITY, ID, META>,
        options: SqlUpdateOptions
    ): JdbcRunner<Int> {
        return SqlUpdateJdbcRunner(context, options)
    }

    override fun templateExecuteQuery(
        sql: String,
        data: Any,
        options: TemplateExecuteOptions
    ): JdbcRunner<Int> {
        return TemplateExecuteJdbcRunner(sql, data, options)
    }

    override fun <T, R> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions,
        collect: suspend (Flow<T>) -> R
    ): JdbcRunner<R> {
        return TemplateSelectJdbcRunner(sql, data, transform, options, collect)
    }
}
