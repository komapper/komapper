package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class EntityInsertStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: EntityInsertContext<ENTITY, ID, META>,
    val entity: ENTITY
) {
    private val builder = EntityMultiInsertStatementBuilderImpl(dialect, context, listOf(entity))

    fun build(): Statement {
        return builder.build()
    }
}
