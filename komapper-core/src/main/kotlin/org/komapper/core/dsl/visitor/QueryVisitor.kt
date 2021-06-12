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

@ThreadSafe
interface QueryVisitor {

    fun <T, S> plusQuery(left: Query<T>, right: Query<S>): QueryRunner

    fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): QueryRunner

    fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    entitySelectQuery(
        context: EntitySelectContext<ENTITY, ID, META>,
        option: EntitySelectOption,
        transform: suspend (Flow<ENTITY>) -> R
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        option: EntityDeleteBatchOption,
        entities: List<ENTITY>
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        option: EntityDeleteOption,
        entity: ENTITY
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        option: EntityInsertOption,
        entities: List<ENTITY>
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        option: EntityInsertBatchOption,
        entities: List<ENTITY>
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        option: EntityInsertOption,
        entity: ENTITY
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        option: EntityUpdateBatchOption,
        entities: List<ENTITY>
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        option: EntityUpdateOption,
        entity: ENTITY
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        option: InsertOption,
        entities: List<ENTITY>
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        option: InsertOption,
        entities: List<ENTITY>
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        option: InsertOption,
        entity: ENTITY,
    ): QueryRunner

    fun schemaCreateQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        option: SchemaCreateOption
    ): QueryRunner

    fun schemaDropQuery(
        entityMetamodels: List<EntityMetamodel<*, *, *>>,
        option: SchemaDropOption
    ): QueryRunner

    fun schemaDropAllQuery(option: SchemaDropAllOption): QueryRunner

    fun scriptExecuteQuery(
        sql: String,
        option: ScriptExecuteOption
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        option: SqlSelectOption,
        collect: suspend (Flow<ENTITY>) -> R
    ): QueryRunner

    fun <T : Any, R>
    sqlSetOperationQuery(
        context: SqlSetOperationContext<T>,
        option: SqlSetOperationOption,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): QueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    sqlPairEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        option: SqlSelectOption,
        metamodels: Pair<A_META, B_META>,
        collect: suspend (Flow<Pair<A, B?>>) -> R
    ): QueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    sqlPairEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Pair<A, B?>>,
        option: SqlSetOperationOption,
        metamodels: Pair<A_META, B_META>,
        collect: suspend (Flow<Pair<A, B?>>) -> R
    ): QueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>, R>
    sqlTripleEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        option: SqlSelectOption,
        metamodels: Triple<A_META, B_META, C_META>,
        collect: suspend (Flow<Triple<A, B?, C?>>) -> R
    ): QueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>, R>
    sqlTripleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Triple<A, B?, C?>>,
        option: SqlSetOperationOption,
        metamodels: Triple<A_META, B_META, C_META>,
        collect: suspend (Flow<Triple<A, B?, C?>>) -> R
    ): QueryRunner

    fun <R> sqlMultipleEntitiesQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        metamodels: List<EntityMetamodel<*, *, *>>,
        collect: suspend (Flow<Entities>) -> R
    ): QueryRunner

    fun <R> sqlMultipleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Entities>,
        option: SqlSetOperationOption,
        metamodels: List<EntityMetamodel<*, *, *>>,
        collect: suspend (Flow<Entities>) -> R
    ): QueryRunner

    fun <A : Any, R>
    sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): QueryRunner

    fun <A : Any, R>
    sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext<A?>,
        option: SqlSetOperationOption,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): QueryRunner

    fun <A : Any, B : Any, R>
    sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): QueryRunner

    fun <A : Any, B : Any, R>
    sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext<Pair<A?, B?>>,
        option: SqlSetOperationOption = SqlSetOperationOption.default,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): QueryRunner

    fun <A : Any, B : Any, C : Any, R>
    sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): QueryRunner

    fun <A : Any, B : Any, C : Any, R>
    sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Triple<A?, B?, C?>>,
        option: SqlSetOperationOption,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): QueryRunner

    fun <R> sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): QueryRunner

    fun <R> sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Columns>,
        option: SqlSetOperationOption,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Columns>) -> R
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    sqlDeleteQuery(
        context: SqlDeleteContext<ENTITY, ID, META>,
        option: SqlDeleteOption
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    sqlInsertQuery(
        context: SqlInsertContext<ENTITY, ID, META>,
        option: SqlInsertOption
    ): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    sqlUpdateQuery(
        context: SqlUpdateContext<ENTITY, ID, META>,
        option: SqlUpdateOption
    ): QueryRunner

    fun templateExecuteQuery(
        sql: String,
        params: Any,
        option: TemplateExecuteOption
    ): QueryRunner

    fun <T, R> templateSelectQuery(
        sql: String,
        params: Any,
        transform: (Row) -> T,
        option: TemplateSelectOption,
        collect: suspend (Flow<T>) -> R
    ): QueryRunner
}
