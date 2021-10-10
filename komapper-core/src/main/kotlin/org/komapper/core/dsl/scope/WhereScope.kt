package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.expression.Criterion

@Scope
class WhereScope(
    private val support: FilterScopeSupport<WhereScope> = FilterScopeSupport { WhereScope() }
) : FilterScope by support,
    List<Criterion> by support {

    fun and(declaration: WhereDeclaration) {
        support.addCriteria(declaration, Criterion::And)
    }

    fun or(declaration: WhereDeclaration) {
        support.addCriteria(declaration, Criterion::Or)
    }

    fun not(declaration: WhereDeclaration) {
        support.addCriteria(declaration, Criterion::Not)
    }
}
