package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlMultipleEntitiesQuery(
    private val context: SqlSelectContext<*, *, *>,
    private val options: SqlSelectOptions,
    private val metamodels: List<EntityMetamodel<*, *, *>>
) : FlowableSubquery<Entities> {

    override val subqueryContext: SubqueryContext<Entities> = SubqueryContext.SqlSelect(context)

    private val support: FlowableSubquerySupport<Entities> =
        FlowableSubquerySupport(subqueryContext) { SqlMultipleEntitiesSetOperationQuery(it, metamodels = metamodels) }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlMultipleEntitiesQuery(context, options, metamodels) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Entities>) -> R): Query<R> = Query { visitor ->
        visitor.sqlMultipleEntitiesQuery(context, options, metamodels, collect)
    }

    override fun asFlowQuery(): FlowQuery<Entities> = FlowQuery { visitor ->
        visitor.sqlMultipleEntitiesQuery(context, options, metamodels)
    }

    override fun except(other: Subquery<Entities>): FlowableSetOperationQuery<Entities> {
        return support.except(other)
    }

    override fun intersect(other: Subquery<Entities>): FlowableSetOperationQuery<Entities> {
        return support.intersect(other)
    }

    override fun union(other: Subquery<Entities>): FlowableSetOperationQuery<Entities> {
        return support.union(other)
    }

    override fun unionAll(other: Subquery<Entities>): FlowableSetOperationQuery<Entities> {
        return support.unionAll(other)
    }
}
