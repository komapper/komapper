package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.declaration.ValuesDeclaration
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlInsertOptions
import org.komapper.core.dsl.scope.ValuesScope
import org.komapper.core.dsl.visitor.QueryVisitor

interface SqlInsertQuery<ENTITY : Any> : Query<Pair<Int, Long?>> {
    fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQuery<ENTITY>
    fun <T : Any> select(block: () -> Subquery<T>): SqlInsertQuery<ENTITY>
    fun options(configure: (SqlInsertOptions) -> SqlInsertOptions): SqlInsertQuery<ENTITY>
}

internal data class SqlInsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlInsertContext<ENTITY, ID, META>,
    private val options: SqlInsertOptions = SqlInsertOptions.default
) : SqlInsertQuery<ENTITY> {

    override fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQuery<ENTITY> {
        val scope = ValuesScope<ENTITY>().apply(declaration)
        val values = when (val values = context.values) {
            is Values.Pairs -> Values.Pairs(values.pairs + scope)
            is Values.Subquery -> Values.Pairs(scope.toList())
        }
        val newContext = context.copy(values = values)
        return copy(context = newContext)
    }

    override fun <T : Any> select(block: () -> Subquery<T>): SqlInsertQuery<ENTITY> {
        val subquery = block()
        val values = Values.Subquery(subquery)
        val newContext = context.copy(values = values)
        return copy(context = newContext)
    }

    override fun options(configure: (SqlInsertOptions) -> SqlInsertOptions): SqlInsertQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlInsertQuery(context, options)
    }
}
