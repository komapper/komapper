package org.komapper.core.dsl.query

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaCreateOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaCreateQuery : Query<Unit> {
    fun option(configure: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery
}

internal data class SchemaCreateQueryImpl(
    private val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    private val option: SchemaCreateOption = SchemaCreateOption.default
) : SchemaCreateQuery {

    override fun option(configure: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.schemaCreateQuery(entityMetamodels, option)
    }
}
