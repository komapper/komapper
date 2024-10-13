package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface RelationUpdateReturningQuery<T> : Query<T> {
    fun options(configure: (UpdateOptions) -> UpdateOptions): RelationUpdateReturningQuery<T>
}

internal data class RelationUpdateReturningQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
) : RelationUpdateReturningQuery<List<ENTITY>> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): RelationUpdateReturningQuery<List<ENTITY>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationUpdateReturningQuery(context)
    }
}

internal data class RelationUpdateReturningSingleColumnQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
    private val expression: ColumnExpression<A, *>,
) : RelationUpdateReturningQuery<List<A?>> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): RelationUpdateReturningQuery<List<A?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationUpdateReturningSingleColumnQuery(context, expression)
    }
}

internal data class RelationUpdateReturningPairColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
) : RelationUpdateReturningQuery<List<Pair<A?, B?>>> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): RelationUpdateReturningQuery<List<Pair<A?, B?>>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationUpdateReturningPairColumnsQuery(context, expressions)
    }
}

internal data class RelationUpdateReturningTripleColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
) : RelationUpdateReturningQuery<List<Triple<A?, B?, C?>>> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): RelationUpdateReturningQuery<List<Triple<A?, B?, C?>>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationUpdateReturningTripleColumnsQuery(context, expressions)
    }
}
