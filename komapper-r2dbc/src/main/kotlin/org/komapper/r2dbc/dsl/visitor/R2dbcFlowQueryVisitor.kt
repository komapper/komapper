package org.komapper.r2dbc.dsl.visitor

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.r2dbc.dsl.runner.FlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcRowTransformers
import org.komapper.r2dbc.dsl.runner.SelectFlowBuilder
import org.komapper.r2dbc.dsl.runner.SetOperationFlowBuilder
import org.komapper.r2dbc.dsl.runner.TemplateSelectFlowBuilder

internal object R2dbcFlowQueryVisitor : FlowQueryVisitor<FlowBuilder<*>> {

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> selectQuery(
        context: SelectContext<ENTITY, ID, META>
    ): FlowBuilder<ENTITY> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return SelectFlowBuilder(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<ENTITY, ID, META>
    ): FlowBuilder<ENTITY> {
        val transform = R2dbcRowTransformers.singleEntity(metamodel)
        return SetOperationFlowBuilder(context, transform)
    }

    override fun <A : Any> singleColumnQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>
    ): FlowBuilder<A?> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SelectFlowBuilder(context, transform)
    }

    override fun <A : Any> singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>
    ): FlowBuilder<A?> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return SetOperationFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any> pairColumnsQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): FlowBuilder<Pair<A?, B?>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SelectFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): FlowBuilder<Pair<A?, B?>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return SetOperationFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any, C : Any> tripleColumnsQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): FlowBuilder<Triple<A?, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return SelectFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any, C : Any> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): FlowBuilder<Triple<A?, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return SetOperationFlowBuilder(context, transform)
    }

    override fun multipleColumnsQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>
    ): FlowBuilder<Columns> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SelectFlowBuilder(context, transform)
    }

    override fun multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>
    ): FlowBuilder<Columns> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return SetOperationFlowBuilder(context, transform)
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
