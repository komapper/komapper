package org.komapper.r2dbc.dsl.visitor

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.Entities
import org.komapper.core.dsl.runner.FlowQueryRunner
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.r2dbc.dsl.runner.ResultRowTransformers
import org.komapper.r2dbc.dsl.runner.SqlSelectFlowQueryRunner
import org.komapper.r2dbc.dsl.runner.SqlSetOperationFlowQueryRunner

internal class R2dbcFlowQueryVisitor : FlowQueryVisitor {

    override fun <T : Any> sqlSelectQuery(
        context: SqlSelectContext<T, *, *>,
        options: SqlSelectOptions
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.singleEntity(context.target)
        return SqlSelectFlowQueryRunner(context, options, transform)
    }

    override fun <T : Any> sqlSetOperationQuery(
        context: SqlSetOperationContext<T>,
        options: SqlSetOperationOptions,
        metamodel: EntityMetamodel<T, *, *>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.singleEntity(metamodel)
        return SqlSetOperationFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>> sqlPairEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Pair<A_META, B_META>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.pairEntities(metamodels)
        return SqlSelectFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>> sqlPairEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Pair<A, B?>>,
        options: SqlSetOperationOptions,
        metamodels: Pair<A_META, B_META>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.pairEntities(metamodels)
        return SqlSetOperationFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>> sqlTripleEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        options: SqlSelectOptions,
        metamodels: Triple<A_META, B_META, C_META>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.tripleEntities(metamodels)
        return SqlSelectFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>> sqlTripleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Triple<A, B?, C?>>,
        options: SqlSetOperationOptions,
        metamodels: Triple<A_META, B_META, C_META>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.tripleEntities(metamodels)
        return SqlSetOperationFlowQueryRunner(context, options, transform)
    }

    override fun sqlMultipleEntitiesQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        metamodels: List<EntityMetamodel<*, *, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.multipleEntities(metamodels)
        return SqlSelectFlowQueryRunner(context, options, transform)
    }

    override fun sqlMultipleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Entities>,
        options: SqlSetOperationOptions,
        metamodels: List<EntityMetamodel<*, *, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.multipleEntities(metamodels)
        return SqlSetOperationFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expression: ColumnExpression<A, *>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.singleColumn(expression)
        return SqlSelectFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext<A?>,
        options: SqlSetOperationOptions,
        expression: ColumnExpression<A, *>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.singleColumn(expression)
        return SqlSetOperationFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any, B : Any> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.pairColumns(expressions)
        return SqlSelectFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any, B : Any> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext<Pair<A?, B?>>,
        options: SqlSetOperationOptions,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.pairColumns(expressions)
        return SqlSetOperationFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any, B : Any, C : Any> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.tripleColumns(expressions)
        return SqlSelectFlowQueryRunner(context, options, transform)
    }

    override fun <A : Any, B : Any, C : Any> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Triple<A?, B?, C?>>,
        options: SqlSetOperationOptions,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.tripleColumns(expressions)
        return SqlSetOperationFlowQueryRunner(context, options, transform)
    }

    override fun sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        options: SqlSelectOptions,
        expressions: List<ColumnExpression<*, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.multipleColumns(expressions)
        return SqlSelectFlowQueryRunner(context, options, transform)
    }

    override fun sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Columns>,
        options: SqlSetOperationOptions,
        expressions: List<ColumnExpression<*, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.multipleColumns(expressions)
        return SqlSetOperationFlowQueryRunner(context, options, transform)
    }
}
