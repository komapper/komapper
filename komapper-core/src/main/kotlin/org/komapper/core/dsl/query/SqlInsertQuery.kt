package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.ValuesDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlInsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface SqlInsertQuery<ENTITY : Any, ID> : Query<Pair<Int, ID?>> {
    fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQuery<ENTITY, ID>
    fun <T : Any> select(block: () -> SubqueryExpression<T>): SqlInsertQuery<ENTITY, ID>
    fun options(configure: (SqlInsertOptions) -> SqlInsertOptions): SqlInsertQuery<ENTITY, ID>
}

internal data class SqlInsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlInsertContext<ENTITY, ID, META>,
    private val options: SqlInsertOptions = SqlInsertOptions.default
) : SqlInsertQuery<ENTITY, ID> {

    override fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQuery<ENTITY, ID> {
        val values = when (val values = context.values) {
            is Values.Declarations -> Values.Declarations(values.declarations + declaration)
            is Values.Subquery -> Values.Declarations(listOf(declaration))
        }
        val newContext = context.copy(values = values)
        return copy(context = newContext)
    }

    override fun <T : Any> select(block: () -> SubqueryExpression<T>): SqlInsertQuery<ENTITY, ID> {
        val subquery = block()
        val values = Values.Subquery<ENTITY>(subquery)
        val newContext = context.copy(values = values)
        return copy(context = newContext)
    }

    override fun options(configure: (SqlInsertOptions) -> SqlInsertOptions): SqlInsertQuery<ENTITY, ID> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlInsertQuery(context, options)
    }
}
