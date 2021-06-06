package org.komapper.core.dsl.query

import org.komapper.core.dsl.metamodel.EntityMetamodel

interface QueryVisitor {

    fun <T, S> visit(query: Query.Plus<T, S>): QueryRunner

    fun <T, S> visit(query: Query.FlatMap<T, S>): QueryRunner

    fun <T, S> visit(query: Query.FlatZip<T, S>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntitySelectQueryImpl<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    visit(query: EntitySelectQueryImpl.Collect<ENTITY, ID, META, R>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityDeleteBatchQuery<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityDeleteSingleQuery<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityInsertMultipleQuery<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityInsertBatchQuery<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityInsertSingleQuery<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpdateBatchQuery<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpdateSingleQuery<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpsertBatchQuery<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpsertMultipleQuery<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntityUpsertSingleQuery<ENTITY, ID, META>): QueryRunner

    fun visit(query: SchemaCreateQueryImpl): QueryRunner
    fun visit(query: SchemaDropQueryImpl): QueryRunner
    fun visit(query: SchemaDropAllQueryImpl): QueryRunner
    fun visit(query: ScriptExecuteQueryImpl): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: SqlSelectQueryImpl<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>
    visit(query: SqlSelectQueryImpl.Collect<ENTITY, ID, META, R>): QueryRunner

    fun <T : Any>
    visit(query: SqlSetOperationQueryImpl<T>): QueryRunner

    fun <T : Any, R>
    visit(query: SqlSetOperationQueryImpl.Collect<T, R>): QueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>
    visit(query: SqlPairEntitiesQuery<A, A_META, B, B_META>): QueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    visit(query: SqlPairEntitiesQuery.Collect<A, A_META, B, B_META, R>): QueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>
    visit(query: SqlPairEntitiesSetOperationQuery<A, A_META, B, B_META>): QueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>
    visit(query: SqlPairEntitiesSetOperationQuery.Collect<A, A_META, B, B_META, R>): QueryRunner

    fun <A : Any, A_META : EntityMetamodel<A, *, A_META>,
        B : Any, B_META : EntityMetamodel<B, *, B_META>,
        C : Any, C_META : EntityMetamodel<C, *, C_META>>
    visit(query: SqlTripleEntitiesQuery<A, A_META, B, B_META, C, C_META>): QueryRunner

    fun visit(query: SqlMultipleEntitiesQuery): QueryRunner

    fun <A : Any>
    visit(query: SqlSingleColumnQuery<A>): QueryRunner

    fun <A : Any, R>
    visit(query: SqlSingleColumnQuery.Collect<A, R>): QueryRunner

    fun <A : Any>
    visit(query: SqlSingleColumnSetOperationQuery<A>): QueryRunner

    fun <A : Any, R>
    visit(query: SqlSingleColumnSetOperationQuery.Collect<A, R>): QueryRunner

    fun <A : Any, B : Any>
    visit(query: SqlPairColumnsQuery<A, B>): QueryRunner

    fun <A : Any, B : Any, R>
    visit(query: SqlPairColumnsQuery.Collect<A, B, R>): QueryRunner

    fun <A : Any, B : Any>
    visit(query: SqlPairColumnsSetOperationQuery<A, B>): QueryRunner

    fun <A : Any, B : Any, R>
    visit(query: SqlPairColumnsSetOperationQuery.Collect<A, B, R>): QueryRunner

    fun <A : Any, B : Any, C : Any>
    visit(query: SqlTripleColumnsQuery<A, B, C>): QueryRunner

    fun <A : Any, B : Any, C : Any, R>
    visit(query: SqlTripleColumnsQuery.Collect<A, B, C, R>): QueryRunner

    fun <A : Any, B : Any, C : Any>
    visit(query: SqlTripleColumnsSetOperationQuery<A, B, C>): QueryRunner

    fun <A : Any, B : Any, C : Any, R>
    visit(query: SqlTripleColumnsSetOperationQuery.Collect<A, B, C, R>): QueryRunner

    fun visit(query: SqlMultipleColumnsQuery): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: SqlDeleteQueryImpl<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: SqlInsertQueryImpl<ENTITY, ID, META>): QueryRunner

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: SqlUpdateQueryImpl<ENTITY, ID, META>): QueryRunner

    fun visit(query: TemplateExecuteQueryImpl): QueryRunner

    fun <T> visit(query: TemplateSelectQueryImpl<T>): QueryRunner

    fun <T, R> visit(query: TemplateSelectQueryImpl.Collect<T, R>): QueryRunner
}
