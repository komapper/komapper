package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.SetDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression

@ThreadSafe
interface RelationInsertQueryBuilder<T : Any, ID> {
    fun values(declaration: SetDeclaration<T>): RelationInsertQuery<T, ID>
    fun select(block: () -> SubqueryExpression<T>): RelationInsertQuery<T, ID>
}

internal class RelationInsertQueryBuilderImpl<T : Any, ID>(private val query: RelationInsertQuery<T, ID>) : RelationInsertQueryBuilder<T, ID> {
    override fun values(declaration: SetDeclaration<T>): RelationInsertQuery<T, ID> {
        return query.values(declaration)
    }

    override fun select(block: () -> SubqueryExpression<T>): RelationInsertQuery<T, ID> {
        return query.select(block)
    }
}
