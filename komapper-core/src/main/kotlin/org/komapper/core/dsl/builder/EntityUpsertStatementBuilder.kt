package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty

interface EntityUpsertStatementBuilder<ENTITY : Any> {
    fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement
}

/**
 *  This class emulates the "INSERT .. ON DUPLICATE KEY UPDATE" functionality.
 */
class EntityMergeStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    @Suppress("unused") private val dialect: BuilderDialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val insertStatementBuilder: EntityInsertStatementBuilder<ENTITY, ID, META>,
    private val upsertStatementBuilder: EntityUpsertStatementBuilder<ENTITY>,
) : EntityUpsertStatementBuilder<ENTITY> {
    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val keys = context.keys.ifEmpty { context.target.idProperties() }
        val autoIncrementProperty = context.target.getAutoIncrementProperty()
        return if (autoIncrementProperty != null && autoIncrementProperty in keys) {
            // fallback to the insert statement
            insertStatementBuilder.build()
        } else {
            upsertStatementBuilder.build(assignments)
        }
    }
}
