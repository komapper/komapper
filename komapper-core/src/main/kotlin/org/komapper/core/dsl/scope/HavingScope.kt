package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.HavingDeclaration

@Scope
class HavingScope(
    private val support: FilterScopeSupport<HavingScope> = FilterScopeSupport { HavingScope() }
) : FilterScope by support,
    List<Criterion> by support {

    fun and(declaration: HavingDeclaration) {
        support.addCriteria(declaration, Criterion::And)
    }

    fun or(declaration: HavingDeclaration) {
        support.addCriteria(declaration, Criterion::Or)
    }

    fun not(declaration: HavingDeclaration) {
        support.addCriteria(declaration, Criterion::Not)
    }
}
