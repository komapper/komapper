package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to drop tables and their associated constraints.
 * This query returns Unit.
 */
interface SchemaDropQuery : Query<Unit> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
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
