package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.OnDeclaration

/**
 * Provides operators for the ON clause.
 */
@Scope
class OnScope(
    private val support: FilterScopeSupport,
) : FilterScope by support {

    fun and(declaration: OnDeclaration) {
        addCriteria(declaration, Criterion::And)
    }

    fun or(declaration: OnDeclaration) {
        addCriteria(declaration, Criterion::Or)
    }

    fun not(declaration: OnDeclaration) {
        addCriteria(declaration, Criterion::Not)
    }

    private fun addCriteria(declaration: OnDeclaration, operator: (List<Criterion>) -> Criterion) {
        val newSupport = FilterScopeSupport()
        OnScope(newSupport).apply(declaration)
        val criterion = operator(newSupport.toList())
        support.add(criterion)
    }
}
