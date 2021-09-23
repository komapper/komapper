package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.declaration.OnDeclaration
import org.komapper.core.dsl.element.Criterion

@Scope
class OnScope(
    private val support: FilterScopeSupport<OnScope> = FilterScopeSupport { OnScope() }
) : FilterScope by support,
    List<Criterion> by support {

    fun and(declaration: OnDeclaration) {
        support.addCriteria(declaration, Criterion::And)
    }

    fun or(declaration: OnDeclaration) {
        support.addCriteria(declaration, Criterion::Or)
    }

    fun not(declaration: OnDeclaration) {
        support.addCriteria(declaration, Criterion::Not)
    }
}
