package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.declaration.WhenDeclaration
import org.komapper.core.dsl.element.Criterion

@Scope
class WhenScope(
    private val support: FilterScopeSupport<WhenScope> = FilterScopeSupport { WhenScope() }
) : FilterScope by support,
    List<Criterion> by support {

    fun and(declaration: WhenDeclaration) {
        support.addCriteria(declaration, Criterion::And)
    }

    fun or(declaration: WhenDeclaration) {
        support.addCriteria(declaration, Criterion::Or)
    }

    fun not(declaration: WhenDeclaration) {
        support.addCriteria(declaration, Criterion::Not)
    }
}
