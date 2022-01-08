package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to create tables and their associated constraints.
 * This query returns Unit.
 */
interface SchemaCreateQuery : Query<Unit> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
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
