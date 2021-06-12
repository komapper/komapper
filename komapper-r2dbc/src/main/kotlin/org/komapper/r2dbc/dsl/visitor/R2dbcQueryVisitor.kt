package org.komapper.r2dbc.dsl.visitor

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
import org.komapper.core.dsl.option.EntityDeleteBatchOption
import org.komapper.core.dsl.option.EntityDeleteOption
import org.komapper.core.dsl.option.EntityInsertBatchOption
import org.komapper.core.dsl.option.EntityInsertOption
import org.komapper.core.dsl.option.EntitySelectOption
import org.komapper.core.dsl.option.EntityUpdateBatchOption
import org.komapper.core.dsl.option.EntityUpdateOption
import org.komapper.core.dsl.option.InsertOption
import org.komapper.core.dsl.option.SchemaCreateOption
import org.komapper.core.dsl.option.SchemaDropAllOption
import org.komapper.core.dsl.option.SchemaDropOption
import org.komapper.core.dsl.option.ScriptExecuteOption
import org.komapper.core.dsl.option.SqlDeleteOption
import org.komapper.core.dsl.option.SqlInsertOption
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.core.dsl.option.SqlUpdateOption
import org.komapper.core.dsl.option.TemplateExecuteOption
import org.komapper.core.dsl.option.TemplateSelectOption
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Entities
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.r2dbc.dsl.runner.EntityDeleteSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertMultipleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityInsertSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntitySelectQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityUpdateSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertMultipleQueryRunner
import org.komapper.r2dbc.dsl.runner.EntityUpsertSingleQueryRunner
import org.komapper.r2dbc.dsl.runner.R2dbcQueryRunner
import org.komapper.r2dbc.dsl.runner.ResultRowTransformers
import org.komapper.r2dbc.dsl.runner.SchemaCreateQueryRunner
import org.komapper.r2dbc.dsl.runner.SchemaDropAllQueryRunner
import org.komapper.r2dbc.dsl.runner.SchemaDropQueryRunner
import org.komapper.r2dbc.dsl.runner.ScriptExecuteQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlDeleteQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlInsertQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlSelectQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlSetOperationQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlUpdateQueryRunner
import org.komapper.r2dbc.dsl.runner.TemplateExecuteQueryRunner
import org.komapper.r2dbc.dsl.runner.TemplateSelectQueryRunner

internal class R2dbcQueryVisitor : QueryVisitor {

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> plusQuery(left: Query<T>, right: Query<S>): QueryRunner {
        val leftRunner = left.accept(this) as R2dbcQueryRunner<T>
        val rightRunner = right.accept(this) as R2dbcQueryRunner<S>
        return R2dbcQueryRunner.Plus(leftRunner, rightRunner)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): QueryRunner {
        val runner = query.accept(this) as R2dbcQueryRunner<T>
        return R2dbcQueryRunner.FlatMap(runner) {
            transform(it).accept(this@R2dbcQueryVisitor) as R2dbcQueryRunner<S>
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): QueryRunner {
        val runner = query.accept(this) as R2dbcQueryRunner<T>
        return R2dbcQueryRunner.FlatZip(runner) {
            transform(it).accept(this@R2dbcQueryVisitor) as R2dbcQueryRunner<S>
        }
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: EntitySelectContext<ENTITY, ID, META>,
        option: EntitySelectOption,
        transform: suspend (Flow<ENTITY>) -> R
    ): QueryRunner {
        return EntitySelectQueryRunner(context, option, transform)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        option: EntityDeleteBatchOption,
        entities: List<ENTITY>
    ): QueryRunner {
        throw UnsupportedOperationException("Batch delete is not supported.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        option: EntityDeleteOption,
        entity: ENTITY
    ): QueryRunner {
        return EntityDeleteSingleQueryRunner(context, option, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        option: EntityInsertOption,
        entities: List<ENTITY>
    ): QueryRunner {
        return EntityInsertMultipleQueryRunner(context, option, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        option: EntityInsertBatchOption,
        entities: List<ENTITY>
    ): QueryRunner {
        throw UnsupportedOperationException("Batch insert is not supported. Instead, use multiple insert.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        option: EntityInsertOption,
        entity: ENTITY
    ): QueryRunner {
        return EntityInsertSingleQueryRunner(context, option, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        option: EntityUpdateBatchOption,
        entities: List<ENTITY>
    ): QueryRunner {
        throw UnsupportedOperationException("Batch update is not supported. Instead, use multiple update.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        option: EntityUpdateOption,
        entity: ENTITY
    ): QueryRunner {
        return EntityUpdateSingleQueryRunner(context, option, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        option: InsertOption,
        entities: List<ENTITY>
    ): QueryRunner {
        throw UnsupportedOperationException("Batch upsert is not supported. Instead, use multiple upsert.")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        option: InsertOption,
        entities: List<ENTITY>
    ): QueryRunner {
        return EntityUpsertMultipleQueryRunner(context, option, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        option: InsertOption,
        entity: ENTITY,
    ): QueryRunner {
        return EntityUpsertSingleQueryRunner(context, option, entity)
    }

    override fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        option: SchemaCreateOption
    ): QueryRunner {
        return SchemaCreateQueryRunner(entityMetamodels, option)
    }

    override fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        option: SchemaDropOption
    ): QueryRunner {
        return SchemaDropQueryRunner(entityMetamodels, option)
    }

    override fun schemaDropAllQuery(option: SchemaDropAllOption): QueryRunner {
        return SchemaDropAllQueryRunner(option)
    }

    override fun scriptExecuteQuery(
        sql: String,
        option: ScriptExecuteOption
    ): QueryRunner {
        return ScriptExecuteQueryRunner(sql, option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        option: SqlSelectOption,
        collect: suspend (Flow<ENTITY>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.singleEntity(context.target)
        return SqlSelectQueryRunner(context, option, transform, collect)
    }

    override fun <T : Any, R> sqlSetOperationQuery(
        context: SqlSetOperationContext<T>,
        option: SqlSetOperationOption,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): QueryRunner {
        val provide = ResultRowTransformers.singleEntity(metamodel)
        return SqlSetOperationQueryRunner(context, option, provide, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    sqlPairEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        option: SqlSelectOption,
        metamodels: Pair<A_META, B_META>,
        collect: suspend (Flow<Pair<A, B?>>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.pairEntities(metamodels)
        return SqlSelectQueryRunner(context, option, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    sqlPairEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Pair<A, B?>>,
        option: SqlSetOperationOption,
        metamodels: Pair<A_META, B_META>,
        collect: suspend (Flow<Pair<A, B?>>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.pairEntities(metamodels)
        return SqlSetOperationQueryRunner(context, option, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>, R>
    sqlTripleEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        option: SqlSelectOption,
        metamodels: Triple<A_META, B_META, C_META>,
        collect: suspend (Flow<Triple<A, B?, C?>>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.tripleEntities(metamodels)
        return SqlSelectQueryRunner(context, option, transform, collect)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>, R>
    sqlTripleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Triple<A, B?, C?>>,
        option: SqlSetOperationOption,
        metamodels: Triple<A_META, B_META, C_META>,
        collect: suspend (Flow<Triple<A, B?, C?>>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.tripleEntities(metamodels)
        return SqlSetOperationQueryRunner(context, option, transform, collect)
    }

    override fun <R> sqlMultipleEntitiesQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        metamodels: List<EntityMetamodel<*, *, *>>,
        collect: suspend (Flow<Entities>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.multipleEntities(metamodels)
        return SqlSelectQueryRunner(context, option, transform, collect)
    }

    override fun <R> sqlMultipleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Entities>,
        option: SqlSetOperationOption,
        metamodels: List<EntityMetamodel<*, *, *>>,
        collect: suspend (Flow<Entities>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.multipleEntities(metamodels)
        return SqlSetOperationQueryRunner(context, option, transform, collect)
    }

    override fun <A : Any, R> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.singleColumn(expression)
        return SqlSelectQueryRunner(context, option, transform, collect)
    }

    override fun <A : Any, R> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext<A?>,
        option: SqlSetOperationOption,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.singleColumn(expression)
        return SqlSetOperationQueryRunner(context, option, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.pairColumns(expressions)
        return SqlSelectQueryRunner(context, option, transform, collect)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext<Pair<A?, B?>>,
        option: SqlSetOperationOption,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.pairColumns(expressions)
        return SqlSetOperationQueryRunner(context, option, transform, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): QueryRunner {
        val provide = ResultRowTransformers.tripleColumns(expressions)
        return SqlSelectQueryRunner(context, option, provide, collect)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Triple<A?, B?, C?>>,
        option: SqlSetOperationOption,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.tripleColumns(expressions)
        return SqlSetOperationQueryRunner(context, option, transform, collect)
    }

    override fun <R> sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.multipleColumns(expressions)
        return SqlSelectQueryRunner(context, option, transform, collect)
    }

    override fun <R> sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Columns>,
        option: SqlSetOperationOption,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): QueryRunner {
        val transform = ResultRowTransformers.multipleColumns(expressions)
        return SqlSetOperationQueryRunner(context, option, transform, collect)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlDeleteQuery(
        context: SqlDeleteContext<ENTITY, ID, META>,
        option: SqlDeleteOption
    ): QueryRunner {
        return SqlDeleteQueryRunner(context, option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlInsertQuery(
        context: SqlInsertContext<ENTITY, ID, META>,
        option: SqlInsertOption
    ): QueryRunner {
        return SqlInsertQueryRunner(context, option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlUpdateQuery(
        context: SqlUpdateContext<ENTITY, ID, META>,
        option: SqlUpdateOption
    ): QueryRunner {
        return SqlUpdateQueryRunner(context, option)
    }

    override fun templateExecuteQuery(
        sql: String,
        params: Any,
        option: TemplateExecuteOption
    ): QueryRunner {
        return TemplateExecuteQueryRunner(sql, params, option)
    }

    override fun <T, R> templateSelectQuery(
        sql: String,
        params: Any,
        transform: (Row) -> T,
        option: TemplateSelectOption,
        collect: suspend (Flow<T>) -> R
    ): QueryRunner {
        return TemplateSelectQueryRunner(sql, params, transform, option, collect)
    }
}
