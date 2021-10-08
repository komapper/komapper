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
import org.komapper.r2dbc.dsl.runner.FlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcRowTransformers
import org.komapper.r2dbc.dsl.runner.SqlSelectFlowBuilder
import org.komapper.r2dbc.dsl.runner.SqlSetOperationFlowBuilder
import org.komapper.r2dbc.dsl.runner.TemplateSelectFlowBuilder

internal object R2dbcFlowQueryVisitor : FlowQueryVisitor<FlowBuilder<*>> {

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlSelectQuery(
        context: SqlSelectContext<ENTITY, ID, META>,
        options: SqlSelectOptions
    ): FlowBuilder<ENTITY> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return SqlSelectFlowBuilder(context, options, transform)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> sqlSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        metamodel: EntityMetamodel<ENTITY, ID, META>
    ): FlowBuilder<ENTITY> {
        val transform = R2dbcRowTransformers.singleEntity(metamodel)
        return SqlSetOperationFlowBuilder(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>> sqlPairEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Pair<A_META, B_META>
    ): FlowBuilder<Pair<A, B?>> {
        val transform = R2dbcRowTransformers.pairEntities(metamodels)
        return SqlSelectFlowBuilder(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>> sqlPairEntitiesSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        metamodels: Pair<A_META, B_META>
    ): FlowBuilder<Pair<A, B?>> {
        val transform = R2dbcRowTransformers.pairEntities(metamodels)
        return SqlSetOperationFlowBuilder(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>> sqlTripleEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Triple<A_META, B_META, C_META>
    ): FlowBuilder<Triple<A, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleEntities(metamodels)
        return SqlSelectFlowBuilder(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>> sqlTripleEntitiesSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        metamodels: Triple<A_META, B_META, C_META>
    ): FlowBuilder<Triple<A, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleEntities(metamodels)
        return SqlSetOperationFlowBuilder(context, options, transform)
    }

    override fun sqlMultipleEntitiesQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        metamodels: List<EntityMetamodel<*, *, *>>
    ): FlowBuilder<Entities> {
        val transform = R2dbcRowTransformers.multipleEntities(metamodels)
        return SqlSelectFlowBuilder(context, options, transform)
    }

    override fun sqlMultipleEntitiesSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        metamodels: List<EntityMetamodel<*, *, *>>
    ): FlowBuilder<Entities> {
        val transform = R2dbcRowTransformers.multipleEntities(metamodels)
        return SqlSetOperationFlowBuilder(context, options, transform)
    }

    override fun <A : Any> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expression: ColumnExpression<A, *>
    ): FlowBuilder<A?> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SqlSelectFlowBuilder(context, options, transform)
    }

    override fun <A : Any> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expression: ColumnExpression<A, *>
    ): FlowBuilder<A?> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SqlSetOperationFlowBuilder(context, options, transform)
    }

    override fun <A : Any, B : Any> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): FlowBuilder<Pair<A?, B?>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SqlSelectFlowBuilder(context, options, transform)
    }

    override fun <A : Any, B : Any> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): FlowBuilder<Pair<A?, B?>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SqlSetOperationFlowBuilder(context, options, transform)
    }

    override fun <A : Any, B : Any, C : Any> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): FlowBuilder<Triple<A?, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return SqlSelectFlowBuilder(context, options, transform)
    }

    override fun <A : Any, B : Any, C : Any> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): FlowBuilder<Triple<A?, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return SqlSetOperationFlowBuilder(context, options, transform)
    }

    override fun sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: List<ColumnExpression<*, *>>
    ): FlowBuilder<Columns> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SqlSelectFlowBuilder(context, options, transform)
    }

    override fun sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext,
        options: SqlSetOperationOptions,
        expressions: List<ColumnExpression<*, *>>
    ): FlowBuilder<Columns> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SqlSetOperationFlowBuilder(context, options, transform)
    }

    override fun <T> templateSelectQuery(
        sql: String,
        data: Any,
        transform: (Row) -> T,
        options: TemplateSelectOptions
    ): FlowBuilder<*> {
        return TemplateSelectFlowBuilder(sql, data, transform, options)
    }
}
