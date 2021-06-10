package org.komapper.core.dsl.query

import org.komapper.core.dsl.option.SchemaDropAllOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

interface SchemaDropAllQuery : Query<Unit> {
    fun option(configure: (SchemaDropAllOption) -> SchemaDropAllOption): SchemaDropAllQuery
}

internal data class SchemaDropAllQueryImpl(
    private val option: SchemaDropAllOption = SchemaDropAllOption.default
) : SchemaDropAllQuery {

    override fun option(configure: (SchemaDropAllOption) -> SchemaDropAllOption): SchemaDropAllQuery {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.schemaDropAllQuery(option)
    }
}
