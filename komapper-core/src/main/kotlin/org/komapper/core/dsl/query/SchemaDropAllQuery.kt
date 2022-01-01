package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents the query to drop all tables and their associated constraints.
 * This query returns Unit.
 */
interface SchemaDropAllQuery : Query<Unit> {
    /**
     * Builds a query with the options applied.
     * @param configure the configure function to apply options
     * @return the query
     */
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
