package org.komapper.r2dbc.dsl.visitor

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Entities
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.r2dbc.dsl.runner.R2dbcFlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcRowTransformers
import org.komapper.r2dbc.dsl.runner.SqlSelectR2dbcFlowBuilder
import org.komapper.r2dbc.dsl.runner.SqlSetOperationR2dbcFlowBuilder
import org.komapper.r2dbc.dsl.runner.TemplateSelectR2dbcFlowBuilder

internal object R2dbcFlowQueryVisitor : FlowQueryVisitor<R2dbcFlowBuilder<*>> {

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        options: SqlSelectOptions
    ): R2dbcFlowBuilder<ENTITY> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return SqlSelectR2dbcFlowBuilder(context, options, transform)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlSetOperationQuery(
        context: SqlSetOperationContext<ENTITY>,
        options: SqlSetOperationOptions,
        metamodel: EntityMetamodel<ENTITY, ID, META>
    ): R2dbcFlowBuilder<ENTITY> {
        val transform = R2dbcRowTransformers.singleEntity(metamodel)
        return SqlSetOperationR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>> sqlPairEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Pair<A_META, B_META>
    ): R2dbcFlowBuilder<Pair<A, B?>> {
        val transform = R2dbcRowTransformers.pairEntities(metamodels)
        return SqlSelectR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>> sqlPairEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Pair<A, B?>>,
        options: SqlSetOperationOptions,
        metamodels: Pair<A_META, B_META>
    ): R2dbcFlowBuilder<Pair<A, B?>> {
        val transform = R2dbcRowTransformers.pairEntities(metamodels)
        return SqlSetOperationR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>> sqlTripleEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Triple<A_META, B_META, C_META>
    ): R2dbcFlowBuilder<Triple<A, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleEntities(metamodels)
        return SqlSelectR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>> sqlTripleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Triple<A, B?, C?>>,
        options: SqlSetOperationOptions,
        metamodels: Triple<A_META, B_META, C_META>
    ): R2dbcFlowBuilder<Triple<A, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleEntities(metamodels)
        return SqlSetOperationR2dbcFlowBuilder(context, options, transform)
    }

    override fun sqlMultipleEntitiesQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        metamodels: List<EntityMetamodel<*, *, *>>
    ): R2dbcFlowBuilder<Entities> {
        val transform = R2dbcRowTransformers.multipleEntities(metamodels)
        return SqlSelectR2dbcFlowBuilder(context, options, transform)
    }

    override fun sqlMultipleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Entities>,
        options: SqlSetOperationOptions,
        metamodels: List<EntityMetamodel<*, *, *>>
    ): R2dbcFlowBuilder<Entities> {
        val transform = R2dbcRowTransformers.multipleEntities(metamodels)
        return SqlSetOperationR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expression: ColumnExpression<A, *>
    ): R2dbcFlowBuilder<A?> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SqlSelectR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext<A?>,
        options: SqlSetOperationOptions,
        expression: ColumnExpression<A, *>
    ): R2dbcFlowBuilder<A?> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SqlSetOperationR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any, B : Any> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): R2dbcFlowBuilder<Pair<A?, B?>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SqlSelectR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any, B : Any> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext<Pair<A?, B?>>,
        options: SqlSetOperationOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): R2dbcFlowBuilder<Pair<A?, B?>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SqlSetOperationR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any, B : Any, C : Any> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): R2dbcFlowBuilder<Triple<A?, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return SqlSelectR2dbcFlowBuilder(context, options, transform)
    }

    override fun <A : Any, B : Any, C : Any> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Triple<A?, B?, C?>>,
        options: SqlSetOperationOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): R2dbcFlowBuilder<Triple<A?, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return SqlSetOperationR2dbcFlowBuilder(context, options, transform)
    }

    override fun sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: List<ColumnExpression<*, *>>
    ): R2dbcFlowBuilder<Columns> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SqlSelectR2dbcFlowBuilder(context, options, transform)
    }

    override fun sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Columns>,
        options: SqlSetOperationOptions,
        expressions: List<ColumnExpression<*, *>>
    ): R2dbcFlowBuilder<Columns> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SqlSetOperationR2dbcFlowBuilder(context, options, transform)
    }

    override fun <T> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions
    ): R2dbcFlowBuilder<*> {
        return TemplateSelectR2dbcFlowBuilder(sql, data, transform, options)
    }
}
