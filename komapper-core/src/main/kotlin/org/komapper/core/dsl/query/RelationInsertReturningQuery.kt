package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface RelationInsertReturningQuery<T> : Query<T> {
    fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertReturningQuery<T>
}

internal data class RelationInsertValuesReturningQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertReturningQuery<ENTITY> {
    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertReturningQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertValuesReturningQuery(context)
    }
}

internal data class RelationInsertValuesReturningSingleColumnQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
    private val expression: ColumnExpression<A, *>,
) : RelationInsertReturningQuery<A?> {
    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertReturningQuery<A?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertValuesReturningSingleColumnQuery(context, expression)
    }
}

internal data class RelationInsertValuesReturningPairColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
) : RelationInsertReturningQuery<Pair<A?, B?>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertReturningQuery<Pair<A?, B?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertValuesReturningPairColumnsQuery(context, expressions)
    }
}

internal data class RelationInsertValuesReturningTripleColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
) : RelationInsertReturningQuery<Triple<A?, B?, C?>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertReturningQuery<Triple<A?, B?, C?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertValuesReturningTripleColumnsQuery(context, expressions)
    }
}
