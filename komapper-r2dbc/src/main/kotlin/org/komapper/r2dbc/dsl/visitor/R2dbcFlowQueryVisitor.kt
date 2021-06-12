package org.komapper.r2dbc.dsl.visitor

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.option.SqlSetOperationOption
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
        option: SqlSelectOption
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.singleEntity(context.target)
        return SqlSelectFlowQueryRunner(context, option, transform)
    }

    override fun <T : Any> sqlSetOperationQuery(
        context: SqlSetOperationContext<T>,
        option: SqlSetOperationOption,
        metamodel: EntityMetamodel<T, *, *>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.singleEntity(metamodel)
        return SqlSetOperationFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>> sqlPairEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        option: SqlSelectOption,
        metamodels: Pair<A_META, B_META>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.pairEntities(metamodels)
        return SqlSelectFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>> sqlPairEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Pair<A, B?>>,
        option: SqlSetOperationOption,
        metamodels: Pair<A_META, B_META>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.pairEntities(metamodels)
        return SqlSetOperationFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>> sqlTripleEntitiesQuery(
        context: SqlSelectContext<A, *, A_META>,
        option: SqlSelectOption,
        metamodels: Triple<A_META, B_META, C_META>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.tripleEntities(metamodels)
        return SqlSelectFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, C : Any, C_META : EntityMetamodel<C, *, C_META>> sqlTripleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Triple<A, B?, C?>>,
        option: SqlSetOperationOption,
        metamodels: Triple<A_META, B_META, C_META>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.tripleEntities(metamodels)
        return SqlSetOperationFlowQueryRunner(context, option, transform)
    }

    override fun sqlMultipleEntitiesQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        metamodels: List<EntityMetamodel<*, *, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.multipleEntities(metamodels)
        return SqlSelectFlowQueryRunner(context, option, transform)
    }

    override fun sqlMultipleEntitiesSetOperationQuery(
        context: SqlSetOperationContext<Entities>,
        option: SqlSetOperationOption,
        metamodels: List<EntityMetamodel<*, *, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.multipleEntities(metamodels)
        return SqlSetOperationFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any> sqlSingleColumnQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expression: ColumnExpression<A, *>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.singleColumn(expression)
        return SqlSelectFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any> sqlSingleColumnSetOperationQuery(
        context: SqlSetOperationContext<A?>,
        option: SqlSetOperationOption,
        expression: ColumnExpression<A, *>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.singleColumn(expression)
        return SqlSetOperationFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any, B : Any> sqlPairColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.pairColumns(expressions)
        return SqlSelectFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any, B : Any> sqlPairColumnsSetOperationQuery(
        context: SqlSetOperationContext<Pair<A?, B?>>,
        option: SqlSetOperationOption,
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.pairColumns(expressions)
        return SqlSetOperationFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any, B : Any, C : Any> sqlTripleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.tripleColumns(expressions)
        return SqlSelectFlowQueryRunner(context, option, transform)
    }

    override fun <A : Any, B : Any, C : Any> sqlTripleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Triple<A?, B?, C?>>,
        option: SqlSetOperationOption,
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.tripleColumns(expressions)
        return SqlSetOperationFlowQueryRunner(context, option, transform)
    }

    override fun sqlMultipleColumnsQuery(
        context: SqlSelectContext<*, *, *>,
        option: SqlSelectOption,
        expressions: List<ColumnExpression<*, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.multipleColumns(expressions)
        return SqlSelectFlowQueryRunner(context, option, transform)
    }

    override fun sqlMultipleColumnsSetOperationQuery(
        context: SqlSetOperationContext<Columns>,
        option: SqlSetOperationOption,
        expressions: List<ColumnExpression<*, *>>
    ): FlowQueryRunner {
        val transform = ResultRowTransformers.multipleColumns(expressions)
        return SqlSetOperationFlowQueryRunner(context, option, transform)
    }
}
