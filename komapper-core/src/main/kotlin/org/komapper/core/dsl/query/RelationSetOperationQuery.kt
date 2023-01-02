package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a set operation query.
 *
 * @param ENTITY the entity type
 */
interface RelationSetOperationQuery<ENTITY : Any> : FlowSetOperationQuery<ENTITY>

internal data class RelationSetOperationQueryImpl<ENTITY : Any>(
    override val context: SetOperationContext,
    private val metamodel: EntityMetamodel<ENTITY, *, *>,
) : RelationSetOperationQuery<ENTITY> {

    private val support: SetOperationQuerySupport<ENTITY> = SetOperationQuerySupport(context)

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.setOperationQuery(context, metamodel)
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.setOperationQuery(context, metamodel, collect)
        }
    }

    override fun except(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<ENTITY> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: SortExpression): FlowSetOperationQuery<ENTITY> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configure: (SelectOptions) -> SelectOptions): FlowSetOperationQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }
}
