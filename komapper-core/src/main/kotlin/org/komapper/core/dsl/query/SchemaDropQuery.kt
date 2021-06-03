package org.komapper.core.dsl.query

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaDropOption

interface SchemaDropQuery : Query<Unit> {
    fun option(configure: (SchemaDropOption) -> SchemaDropOption): SchemaDropQuery
}

data class SchemaDropQueryImpl(
    val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    val option: SchemaDropOption = SchemaDropOption.default
) : SchemaDropQuery {

    override fun option(configure: (SchemaDropOption) -> SchemaDropOption): SchemaDropQuery {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
