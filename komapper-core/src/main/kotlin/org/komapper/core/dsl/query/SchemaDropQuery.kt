package org.komapper.core.dsl.query

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaDropOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaDropQuery : Query<Unit> {
    fun option(configure: (SchemaDropOption) -> SchemaDropOption): SchemaDropQuery
}

internal data class SchemaDropQueryImpl(
    private val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    private val option: SchemaDropOption = SchemaDropOption.default
) : SchemaDropQuery {

    override fun option(configure: (SchemaDropOption) -> SchemaDropOption): SchemaDropQuery {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.schemaDropQuery(entityMetamodels, option)
    }
}
