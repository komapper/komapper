package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaCreateQuery : Query<Unit> {
    fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaCreateQuery
}

internal data class SchemaCreateQueryImpl(
    private val context: SchemaContext,
) : SchemaCreateQuery {

    override fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaCreateQuery {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.schemaCreateQuery(context)
    }
}
