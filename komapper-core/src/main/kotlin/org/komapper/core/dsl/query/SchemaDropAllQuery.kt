package org.komapper.core.dsl.query

import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaDropAllQuery : Query<Unit> {
    fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaDropAllQuery
}

internal data class SchemaDropAllQueryImpl(
    private val options: SchemaOptions = SchemaOptions.default
) : SchemaDropAllQuery {

    override fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaDropAllQuery {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.schemaDropAllQuery(options)
    }
}
