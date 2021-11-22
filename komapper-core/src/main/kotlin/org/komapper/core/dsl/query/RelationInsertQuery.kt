package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationInsertContext
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.ValuesDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface RelationInsertQuery<ENTITY : Any, ID> : Query<Pair<Int, ID?>> {
    fun values(declaration: ValuesDeclaration<ENTITY>): RelationInsertQuery<ENTITY, ID>
    fun <T : Any> select(block: () -> SubqueryExpression<T>): RelationInsertQuery<ENTITY, ID>
    fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertQuery<ENTITY, ID>
}

internal data class RelationInsertQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertContext<ENTITY, ID, META>,
    private val options: InsertOptions = InsertOptions.default
) : RelationInsertQuery<ENTITY, ID> {

    override fun values(declaration: ValuesDeclaration<ENTITY>): RelationInsertQuery<ENTITY, ID> {
        val values = when (val values = context.values) {
            is Values.Declarations -> Values.Declarations(values.declarations + declaration)
            is Values.Subquery -> Values.Declarations(listOf(declaration))
        }
        val newContext = context.copy(values = values)
        return copy(context = newContext)
    }

    override fun <T : Any> select(block: () -> SubqueryExpression<T>): RelationInsertQuery<ENTITY, ID> {
        val subquery = block()
        val values = Values.Subquery<ENTITY>(subquery)
        val newContext = context.copy(values = values)
        return copy(context = newContext)
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertQuery<ENTITY, ID> {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertQuery(context, options)
    }
}
