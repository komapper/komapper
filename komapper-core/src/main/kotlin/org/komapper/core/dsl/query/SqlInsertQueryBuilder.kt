package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.declaration.ValuesDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression

@ThreadSafe
interface SqlInsertQueryBuilder<T : Any, ID> {
    fun values(declaration: ValuesDeclaration<T>): SqlInsertQuery<T, ID>
    fun select(block: () -> SubqueryExpression<T>): SqlInsertQuery<T, ID>
}

internal class SqlInsertQueryBuilderImpl<T : Any, ID>(private val query: SqlInsertQuery<T, ID>) : SqlInsertQueryBuilder<T, ID> {
    override fun values(declaration: ValuesDeclaration<T>): SqlInsertQuery<T, ID> {
        return query.values(declaration)
    }

    override fun select(block: () -> SubqueryExpression<T>): SqlInsertQuery<T, ID> {
        return query.select(block)
    }
}
