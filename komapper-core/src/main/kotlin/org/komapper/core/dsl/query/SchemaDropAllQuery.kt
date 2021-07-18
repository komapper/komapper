package org.komapper.core.dsl.query

import org.komapper.core.dsl.options.SchemaDropAllOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaDropAllQuery : Query<Unit> {
    fun options(configure: (SchemaDropAllOptions) -> SchemaDropAllOptions): SchemaDropAllQuery
}

internal data class SchemaDropAllQueryImpl(
    private val options: SchemaDropAllOptions = SchemaDropAllOptions.default
) : SchemaDropAllQuery {

    override fun options(configure: (SchemaDropAllOptions) -> SchemaDropAllOptions): SchemaDropAllQuery {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.schemaDropAllQuery(options)
    }
}
