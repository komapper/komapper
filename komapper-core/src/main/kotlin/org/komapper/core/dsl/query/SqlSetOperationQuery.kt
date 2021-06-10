package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

interface SqlSetOperationQuery<ENTITY : Any> : FlowableSetOperationQuery<ENTITY>

internal data class SqlSetOperationQueryImpl<ENTITY : Any>(
    private val context: SqlSetOperationContext<ENTITY>,
    private val option: SqlSetOperationOption = SqlSetOperationOption.default,
    private val metamodel: EntityMetamodel<ENTITY, *, *>
) : SqlSetOperationQuery<ENTITY>, FlowableQuery<ENTITY> {

    override val subqueryContext = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<ENTITY> = SqlSetOperationQuerySupport(context)

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlSetOperationQuery(context, option, metamodel) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> = Query { visitor ->
        visitor.sqlSetOperationQuery(context, option, metamodel, collect)
    }

    override fun asFlowQuery(): FlowQuery<ENTITY> = FlowQuery { visitor ->
        visitor.sqlSetOperationQuery(context, option, metamodel)
    }

    override fun except(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): SqlSetOperationQuery<ENTITY> {
        return copy(option = configurator(option))
    }
}
