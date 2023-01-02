package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.HavingDeclaration

/**
 * Provides operators for the HAVING clause.
 */
@Scope
class HavingScope(
    private val support: FilterScopeSupport,
) : FilterScope by support {

    fun and(declaration: HavingDeclaration) {
        addCriteria(declaration, Criterion::And)
    }

    fun or(declaration: HavingDeclaration) {
        addCriteria(declaration, Criterion::Or)
    }

    fun not(declaration: HavingDeclaration) {
        addCriteria(declaration, Criterion::Not)
    }

    private fun addCriteria(declaration: HavingDeclaration, operator: (List<Criterion>) -> Criterion) {
        val newSupport = FilterScopeSupport()
        HavingScope(newSupport).apply(declaration)
        val criterion = operator(newSupport.toList())
        support.add(criterion)
    }
}
