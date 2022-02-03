package org.komapper.core.dsl.visitor

import kotlinx.coroutines.flow.Flow
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.Row

@ThreadSafe
interface QueryVisitor<VISIT_RESULT> {

    fun <T, S> andThenQuery(left: Query<T>, right: Query<S>): VISIT_RESULT

    fun <T, S> mapQuery(query: Query<T>, transform: (T) -> S): VISIT_RESULT

    fun <T, S> zipQuery(left: Query<T>, right: Query<S>): VISIT_RESULT

    fun <T, S> flatMapQuery(query: Query<T>, transform: (T) -> Query<S>): VISIT_RESULT

    fun <T, S> flatZipQuery(query: Query<T>, transform: (T) -> Query<S>): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> entityStoreQuery(
        context: SelectContext<ENTITY, ID, META>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    entitySelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteBatchQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityDeleteSingleQuery(
        context: EntityDeleteContext<ENTITY, ID, META>,
        entity: ENTITY
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityInsertMultipleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityInsertBatchQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityInsertSingleQuery(
        context: EntityInsertContext<ENTITY, ID, META>,
        entity: ENTITY
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateBatchQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpdateSingleQuery(
        context: EntityUpdateContext<ENTITY, ID, META>,
        entity: ENTITY
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertBatchQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertMultipleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleUpdateQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    entityUpsertSingleIgnoreQuery(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entity: ENTITY,
    ): VISIT_RESULT

    fun schemaCreateQuery(
        context: SchemaContext
    ): VISIT_RESULT

    fun schemaDropQuery(
        context: SchemaContext
    ): VISIT_RESULT

    fun scriptExecuteQuery(
        context: ScriptContext
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>
    relationSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
        collect: suspend (Flow<ENTITY>) -> R
    ): VISIT_RESULT

    fun <T : Any, R>
    setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<T, *, *>,
        collect: suspend (Flow<T>) -> R
    ): VISIT_RESULT

    fun <A : Any, R>
    singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): VISIT_RESULT

    fun <A : Any, R>
    singleNotNullColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R
    ): VISIT_RESULT

    fun <A : Any, R>
    singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A?>) -> R
    ): VISIT_RESULT

    fun <A : Any, R>
    singleNotNullColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
        collect: suspend (Flow<A>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, R>
    pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, R>
    pairNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, R>
    pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A?, B?>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, R>
    pairNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        collect: suspend (Flow<Pair<A, B>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R>
    tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R>
    tripleNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R>
    tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
    ): VISIT_RESULT

    fun <A : Any, B : Any, C : Any, R>
    tripleNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        collect: suspend (Flow<Triple<A, B, C>>) -> R
    ): VISIT_RESULT

    fun <R> multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R
    ): VISIT_RESULT

    fun <R> multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>,
        collect: suspend (Flow<Record>) -> R
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    relationDeleteQuery(
        context: RelationDeleteContext<ENTITY, ID, META>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    relationInsertValuesQuery(
        context: RelationInsertValuesContext<ENTITY, ID, META>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    relationInsertSelectQuery(
        context: RelationInsertSelectContext<ENTITY, ID, META>
    ): VISIT_RESULT

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
    relationUpdateQuery(
        context: RelationUpdateContext<ENTITY, ID, META>
    ): VISIT_RESULT

    fun templateExecuteQuery(
        context: TemplateExecuteContext
    ): VISIT_RESULT

    fun <T, R> templateSelectQuery(
        context: TemplateSelectContext,
        transform: (Row) -> T,
        collect: suspend (Flow<T>) -> R
    ): VISIT_RESULT
}
