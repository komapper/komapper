package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlTripleEntitiesSetOperationQuery<
    A : Any, A_META : EntityMetamodel<A, *, A_META>,
    B : Any, B_META : EntityMetamodel<B, *, B_META>,
    C : Any, C_META : EntityMetamodel<C, *, C_META>>(
    private val context: SqlSetOperationContext<Triple<A, B?, C?>>,
    private val options: SqlSetOperationOptions = SqlSetOperationOptions.default,
    private val metamodels: Triple<A_META, B_META, C_META>
) : FlowSetOperationQuery<Triple<A, B?, C?>> {

    override val subqueryContext: SubqueryContext<Triple<A, B?, C?>> = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<Triple<A, B?, C?>> = SqlSetOperationQuerySupport(context)

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlTripleEntitiesSetOperationQuery(context, options, metamodels) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlTripleEntitiesSetOperationQuery(context, options, metamodels)
    }

    override fun <R> collect(collect: suspend (Flow<Triple<A, B?, C?>>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlTripleEntitiesSetOperationQuery(context, options, metamodels, collect)
        }
    }

    override fun except(other: Subquery<Triple<A, B?, C?>>): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Triple<A, B?, C?>>): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Triple<A, B?, C?>>): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Triple<A, B?, C?>>): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return copy(options = configurator(options))
    }
}
