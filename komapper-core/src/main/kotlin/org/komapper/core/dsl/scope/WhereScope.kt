package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.WhereDeclaration

/**
 * Provides operators for the WHERE clause.
 */
@Scope
class WhereScope(
    private val support: FilterScopeSupport,
) : FilterScope by support {

    fun and(declaration: WhereDeclaration) {
        addCriteria(declaration, Criterion::And)
    }

    fun or(declaration: WhereDeclaration) {
        addCriteria(declaration, Criterion::Or)
    }

    fun not(declaration: WhereDeclaration) {
        addCriteria(declaration, Criterion::Not)
    }

    private fun addCriteria(declaration: WhereDeclaration, operator: (List<Criterion>) -> Criterion) {
        val newSupport = FilterScopeSupport()
        WhereScope(newSupport).apply(declaration)
        val criterion = operator(newSupport.toList())
        support.add(criterion)
    }
}
