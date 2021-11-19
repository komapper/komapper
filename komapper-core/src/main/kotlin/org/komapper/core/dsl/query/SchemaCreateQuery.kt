package org.komapper.core.dsl.query

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaCreateQuery : Query<Unit> {
    fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaCreateQuery
}

internal data class SchemaCreateQueryImpl(
    private val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    private val options: SchemaOptions = SchemaOptions.default
) : SchemaCreateQuery {

    override fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaCreateQuery {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.schemaCreateQuery(entityMetamodels, options)
    }
}
