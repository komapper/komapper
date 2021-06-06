package org.komapper.core.dsl.query

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaCreateOption

interface SchemaCreateQuery : Query<Unit> {
    fun option(configure: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery
}

data class SchemaCreateQueryImpl(
    val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    val option: SchemaCreateOption = SchemaCreateOption.default
) : SchemaCreateQuery {

    override fun option(configure: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
