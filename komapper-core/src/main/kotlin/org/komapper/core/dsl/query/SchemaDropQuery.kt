package org.komapper.core.dsl.query

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaDropOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaDropQuery : Query<Unit> {
    fun options(configure: (SchemaDropOptions) -> SchemaDropOptions): SchemaDropQuery
}

internal data class SchemaDropQueryImpl(
    private val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    private val options: SchemaDropOptions = SchemaDropOptions.default
) : SchemaDropQuery {

    override fun options(configure: (SchemaDropOptions) -> SchemaDropOptions): SchemaDropQuery {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.schemaDropQuery(entityMetamodels, options)
    }
}
