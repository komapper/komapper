package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.WhenDeclaration

/**
 * Provides operators for the WHEN clause.
 */
@Scope
class WhenScope(
    private val support: FilterScopeSupport,
) : FilterScope by support {

    fun and(declaration: WhenDeclaration) {
        addCriteria(declaration, Criterion::And)
    }

    fun or(declaration: WhenDeclaration) {
        addCriteria(declaration, Criterion::Or)
    }

    fun not(declaration: WhenDeclaration) {
        addCriteria(declaration, Criterion::Not)
    }

    private fun addCriteria(declaration: WhenDeclaration, operator: (List<Criterion>) -> Criterion) {
        val newSupport = FilterScopeSupport()
        WhenScope(newSupport).apply(declaration)
        val criterion = operator(newSupport.toList())
        support.add(criterion)
    }
}
