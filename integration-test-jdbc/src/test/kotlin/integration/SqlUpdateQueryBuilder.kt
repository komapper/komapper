package integration

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.query.SqlUpdateQuery
import org.komapper.core.dsl.scope.SetDeclaration

@ThreadSafe
interface SqlUpdateQueryBuilder<T : Any> {
    fun set(declaration: SetDeclaration<T>): SqlUpdateQuery<T>
}

internal class SqlUpdateQueryBuilderImpl<T : Any>(
    private val query: SqlUpdateQuery<T>
) : SqlUpdateQueryBuilder<T> {
    override fun set(declaration: SetDeclaration<T>): SqlUpdateQuery<T> {
        return query.set(declaration)
    }
}
