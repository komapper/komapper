package org.komapper.core.dsl.visitor

import kotlinx.coroutines.flow.Flow
import org.komapper.core.ThreadSafe
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
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.runner.EntityDeleteBatchRunner
import org.komapper.core.dsl.runner.EntityDeleteSingleRunner
import org.komapper.core.dsl.runner.EntityInsertBatchRunner
import org.komapper.core.dsl.runner.EntityInsertMultipleRunner
import org.komapper.core.dsl.runner.EntityInsertSingleRunner
import org.komapper.core.dsl.runner.EntitySelectRunner
import org.komapper.core.dsl.runner.EntityUpdateBatchRunner
import org.komapper.core.dsl.runner.EntityUpdateSingleRunner
import org.komapper.core.dsl.runner.EntityUpsertBatchRunner
import org.komapper.core.dsl.runner.EntityUpsertMultipleRunner
import org.komapper.core.dsl.runner.EntityUpsertSingleRunner
import org.komapper.core.dsl.runner.Runner
import org.komapper.core.dsl.runner.SchemaCreateRunner
import org.komapper.core.dsl.runner.SchemaDropAllRunner
import org.komapper.core.dsl.runner.SchemaDropRunner
import org.komapper.core.dsl.runner.ScriptExecuteRunner
import org.komapper.core.dsl.runner.SqlDeleteRunner
import org.komapper.core.dsl.runner.SqlInsertRunner
import org.komapper.core.dsl.runner.SqlSelectRunner
import org.komapper.core.dsl.runner.SqlSetOperationRunner
import org.komapper.core.dsl.runner.SqlUpdateRunner
import org.komapper.core.dsl.runner.TemplateExecuteRunner
import org.komapper.core.dsl.runner.TemplateSelectRunner

@ThreadSafe
internal object DefaultQueryVisitor : QueryVisitor<Runner> {

    override fun <T, S> plusQuery(left: Query<T>, right: Query<S>): Runner {
        val leftRunner = left.accept(this)
        val rightRunner = right.accept(this)
        return Runner.Plus(leftRunner, rightRunner)
    }

    override fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): Runner {
        val runner = query.accept(this)
        return Runner.FlatMap(runner)
    }

    override fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): Runner {
        val runner = query.accept(this)
        return Runner.FlatZip(runner)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R> entitySelectQuery(
        context: EntitySelectContext<ENTITY, ID, META>,
        options: EntitySelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): Runner {
        return EntitySelectRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteBatchOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityDeleteBatchRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        options: EntityDeleteOptions,
        entity: ENTITY
    ): Runner {
        return EntityDeleteSingleRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityInsertMultipleRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertBatchOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityInsertBatchRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        options: EntityInsertOptions,
        entity: ENTITY
    ): Runner {
        return EntityInsertSingleRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateBatchOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityUpdateBatchRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        options: EntityUpdateOptions,
        entity: ENTITY
    ): Runner {
        return EntityUpdateSingleRunner(context, options, entity)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityUpsertBatchRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entities: List<ENTITY>
    ): Runner {
        return EntityUpsertMultipleRunner(context, options, entities)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        options: InsertOptions,
        entity: ENTITY,
    ): Runner {
        return EntityUpsertSingleRunner(context, options, entity)
    }

    override fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaCreateOptions
    ): Runner {
        return SchemaCreateRunner(entityMetamodels, options)
    }

    override fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        options: SchemaDropOptions
    ): Runner {
        return SchemaDropRunner(entityMetamodels, options)
    }

    override fun schemaDropAllQuery(options: SchemaDropAllOptions): Runner {
        return SchemaDropAllRunner(options)
    }

    override fun scriptExecuteQuery(
        sql: String,
        options: ScriptExecuteOptions
    ): Runner {
        return ScriptExecuteRunner(sql, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        options: SqlSelectOptions,
        collect: suspend (Flow<ENTITY>) -> R
    ): Runner {
        return SqlSelectRunner(context, options)
    }

    override fun <T : Any, R> sqlSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): Runner {
        return SqlSetOperationRunner(context, options)
    }

    override fun <A : Any, R> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): Runner {
        return SqlSelectRunner(context, options)
    }

    override fun <A : Any, R> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): Runner {
        return SqlSetOperationRunner(context, options)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): Runner {
        return SqlSelectRunner(context, options)
    }

    override fun <A : Any, B : Any, R> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): Runner {
        return SqlSetOperationRunner(context, options)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): Runner {
        return SqlSelectRunner(context, options)
    }

    override fun <A : Any, B : Any, C : Any, R> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): Runner {
        return SqlSetOperationRunner(context, options)
    }

    override fun <R> sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): Runner {
        return SqlSelectRunner(context, options)
    }

    override fun <R> sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): Runner {
        return SqlSetOperationRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlDeleteQuery(
        context: SqlDeleteContext<ENTITY, ID, META>,
        options: SqlDeleteOptions
    ): Runner {
        return SqlDeleteRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlInsertQuery(
        context: SqlInsertContext<ENTITY, ID, META>,
        options: SqlInsertOptions
    ): Runner {
        return SqlInsertRunner(context, options)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlUpdateQuery(
        context: SqlUpdateContext<ENTITY, ID, META>,
        options: SqlUpdateOptions
    ): Runner {
        return SqlUpdateRunner(context, options)
    }

    override fun templateExecuteQuery(
        sql: String,
        data: Any,
        options: TemplateExecuteOptions
    ): Runner {
        return TemplateExecuteRunner(sql, data, options)
    }

    override fun <T, R> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions,
        collect: suspend (Flow<T>) -> R
    ): Runner {
        return TemplateSelectRunner(sql, data, options)
    }
}
