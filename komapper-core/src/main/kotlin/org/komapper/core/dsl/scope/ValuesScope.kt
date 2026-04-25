package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

/**
 * Provides operators for the VALUES table constructor used as a derived table.
 */
@Scope
class ValuesScope<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val target: META,
    private val rows: MutableList<List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>>,
) {
    /**
     * Adds a single row to the VALUES table constructor.
     *
     * @param declaration the per-row property assignments
     */
    fun row(declaration: AssignmentDeclaration<ENTITY, META>) {
        val assignments = mutableListOf<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>()
        val meta = target
        AssignmentScope(assignments).apply { declaration(meta) }
        rows.add(assignments)
    }
}
