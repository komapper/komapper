package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityInsertContext

internal class EntityInsertStatementBuilder<ENTITY : Any>(
    val dialect: Dialect,
    val context: EntityInsertContext<ENTITY>,
    val entity: ENTITY
) {
    private val builder = EntityMultiInsertStatementBuilderImpl(dialect, context, listOf(entity))

    fun build(): Statement {
        return builder.build()
    }
}
