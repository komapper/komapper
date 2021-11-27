package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaDropQuery : Query<Unit> {
    fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaDropQuery
}

internal data class SchemaDropQueryImpl(
    private val context: SchemaContext,
) : SchemaDropQuery {

    override fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaDropQuery {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.schemaDropQuery(context)
    }
}
