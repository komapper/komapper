package org.komapper.core.dsl.query

import org.komapper.core.dsl.option.SchemaDropAllOption

interface SchemaDropAllQuery : Query<Unit> {
    fun option(configure: (SchemaDropAllOption) -> SchemaDropAllOption): SchemaDropAllQuery
}

data class SchemaDropAllQueryImpl(
    val option: SchemaDropAllOption = SchemaDropAllOption.default
) : SchemaDropAllQuery {

    override fun option(configure: (SchemaDropAllOption) -> SchemaDropAllOption): SchemaDropAllQuery {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
