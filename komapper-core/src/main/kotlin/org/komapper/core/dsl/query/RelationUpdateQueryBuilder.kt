package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.AssignmentDeclaration

@ThreadSafe
interface RelationUpdateQueryBuilder<T : Any> {
    fun set(declaration: AssignmentDeclaration<T>): RelationUpdateQuery<T>
}

internal class RelationUpdateQueryBuilderImpl<T : Any>(
    private val query: RelationUpdateQuery<T>
) : RelationUpdateQueryBuilder<T> {
    override fun set(declaration: AssignmentDeclaration<T>): RelationUpdateQuery<T> {
        return query.set(declaration)
    }
}
