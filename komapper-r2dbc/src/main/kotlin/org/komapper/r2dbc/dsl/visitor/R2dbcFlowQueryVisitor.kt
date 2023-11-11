package org.komapper.r2dbc.dsl.visitor

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.r2dbc.dsl.runner.R2dbcFlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcRowTransformers
import org.komapper.r2dbc.dsl.runner.R2dbcSelectFlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcSetOperationFlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcTemplateEntityConversionSelectFlowBuilder
import org.komapper.r2dbc.dsl.runner.R2dbcTemplateSelectFlowBuilder

object R2dbcFlowQueryVisitor : FlowQueryVisitor<R2dbcFlowBuilder<*>> {

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> relationSelectQuery(
        context: SelectContext<ENTITY, ID, META>,
    ): R2dbcFlowBuilder<ENTITY> {
        val transform = R2dbcRowTransformers.singleEntity(context.target)
        return R2dbcSelectFlowBuilder(context, transform)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> setOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<ENTITY, ID, META>,
    ): R2dbcFlowBuilder<ENTITY> {
        val transform = R2dbcRowTransformers.singleEntity(metamodel)
        return R2dbcSetOperationFlowBuilder(context, transform)
    }

    override fun <A : Any> singleColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
    ): R2dbcFlowBuilder<A?> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcSelectFlowBuilder(context, transform)
    }

    override fun <A : Any> singleNotNullColumnSelectQuery(
        context: SelectContext<*, *, *>,
        expression: ColumnExpression<A, *>,
    ): R2dbcFlowBuilder<A> {
        val transform = R2dbcRowTransformers.singleNotNullColumn(expression)
        return R2dbcSelectFlowBuilder(context, transform)
    }

    override fun <A : Any> singleColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
    ): R2dbcFlowBuilder<A?> {
        val transform = R2dbcRowTransformers.singleColumn(expression)
        return R2dbcSetOperationFlowBuilder(context, transform)
    }

    override fun <A : Any> singleNotNullColumnSetOperationQuery(
        context: SetOperationContext,
        expression: ColumnExpression<A, *>,
    ): R2dbcFlowBuilder<*> {
        val transform = R2dbcRowTransformers.singleNotNullColumn(expression)
        return R2dbcSetOperationFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any> pairColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): R2dbcFlowBuilder<Pair<A?, B?>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcSelectFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any> pairNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): R2dbcFlowBuilder<Pair<A, B>> {
        val transform = R2dbcRowTransformers.pairNotNullColumns(expressions)
        return R2dbcSelectFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any> pairColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): R2dbcFlowBuilder<Pair<A?, B?>> {
        val transform = R2dbcRowTransformers.pairColumns(expressions)
        return R2dbcSetOperationFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any> pairNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    ): R2dbcFlowBuilder<Pair<A, B>> {
        val transform = R2dbcRowTransformers.pairNotNullColumns(expressions)
        return R2dbcSetOperationFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any, C : Any> tripleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): R2dbcFlowBuilder<Triple<A?, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcSelectFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any, C : Any> tripleNotNullColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): R2dbcFlowBuilder<Triple<A, B, C>> {
        val transform = R2dbcRowTransformers.tripleNotNullColumns(expressions)
        return R2dbcSelectFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any, C : Any> tripleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): R2dbcFlowBuilder<Triple<A?, B?, C?>> {
        val transform = R2dbcRowTransformers.tripleColumns(expressions)
        return R2dbcSetOperationFlowBuilder(context, transform)
    }

    override fun <A : Any, B : Any, C : Any> tripleNotNullColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    ): R2dbcFlowBuilder<Triple<A, B, C>> {
        val transform = R2dbcRowTransformers.tripleNotNullColumns(expressions)
        return R2dbcSetOperationFlowBuilder(context, transform)
    }

    override fun multipleColumnsSelectQuery(
        context: SelectContext<*, *, *>,
        expressions: List<ColumnExpression<*, *>>,
    ): R2dbcFlowBuilder<Record> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return R2dbcSelectFlowBuilder(context, transform)
    }

    override fun multipleColumnsSetOperationQuery(
        context: SetOperationContext,
        expressions: List<ColumnExpression<*, *>>,
    ): R2dbcFlowBuilder<Record> {
        val transform = R2dbcRowTransformers.multipleColumns(expressions)
        return R2dbcSetOperationFlowBuilder(context, transform)
    }

    override fun <ENTITY : Any> entityConversionSelectQuery(
        context: SelectContext<*, *, *>,
        metamodel: EntityMetamodel<ENTITY, *, *>,
    ): R2dbcFlowBuilder<*> {
        val transform = R2dbcRowTransformers.singleEntity(metamodel)
        return R2dbcSelectFlowBuilder(context, transform)
    }

    override fun <ENTITY : Any> entityConversionSetOperationQuery(
        context: SetOperationContext,
        metamodel: EntityMetamodel<ENTITY, *, *>,
    ): R2dbcFlowBuilder<*> {
        val transform = R2dbcRowTransformers.singleEntity(metamodel)
        return R2dbcSetOperationFlowBuilder(context, transform)
    }

    override fun <T> templateSelectQuery(
        context: TemplateSelectContext,
        transform: (Row) -> T,
    ): R2dbcFlowBuilder<*> {
        return R2dbcTemplateSelectFlowBuilder(context, transform)
    }

    override fun <T : Any> templateEntityConversionSelectQuery(
        context: TemplateSelectContext,
        metamodel: EntityMetamodel<T, *, *>,
    ): R2dbcFlowBuilder<*> {
        val transform = R2dbcRowTransformers.singleEntity(metamodel)
        return R2dbcTemplateEntityConversionSelectFlowBuilder(context, transform)
    }
}
