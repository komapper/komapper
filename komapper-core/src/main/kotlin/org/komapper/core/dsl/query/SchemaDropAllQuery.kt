package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaDropAllQuery : Query<Unit> {
    fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaDropAllQuery
}

internal data class SchemaDropAllQueryImpl(
    private val context: SchemaContext,
) : SchemaDropAllQuery {

    override fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaDropAllQuery {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.schemaDropAllQuery(context)
    }
}
