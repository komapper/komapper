package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption

class SqlTripleEntitiesQuery<
    A : Any, A_META : EntityMetamodel<A, *, A_META>,
    B : Any, B_META : EntityMetamodel<B, *, B_META>,
    C : Any, C_META : EntityMetamodel<C, *, C_META>>(
    val context: SqlSelectContext<A, *, A_META>,
    val option: SqlSelectOption,
    val metamodels: Triple<A_META, B_META, C_META>
) : Subquery<Triple<A, B?, C?>> {

    override val subqueryContext: SubqueryContext<Triple<A, B?, C?>>
        get() = SubqueryContext.SqlSelect(context)

    override fun first(): Query<Triple<A, B?, C?>> {
        TODO("Not yet implemented")
    }

    override fun firstOrNull(): Query<Triple<A, B?, C?>?> {
        TODO("Not yet implemented")
    }

    override fun <R> collect(collect: suspend (Flow<Triple<A, B?, C?>>) -> R): Query<R> {
        TODO("Not yet implemented")
    }

    override fun except(other: Subquery<Triple<A, B?, C?>>): SetOperationQuery<Triple<A, B?, C?>> {
        TODO("Not yet implemented")
    }

    override fun intersect(other: Subquery<Triple<A, B?, C?>>): SetOperationQuery<Triple<A, B?, C?>> {
        TODO("Not yet implemented")
    }

    override fun union(other: Subquery<Triple<A, B?, C?>>): SetOperationQuery<Triple<A, B?, C?>> {
        TODO("Not yet implemented")
    }

    override fun unionAll(other: Subquery<Triple<A, B?, C?>>): SetOperationQuery<Triple<A, B?, C?>> {
        TODO("Not yet implemented")
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
