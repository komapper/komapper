package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Criterion

@Scope
class WhereScope(
    private val support: FilterScopeSupport<WhereScope> = FilterScopeSupport { WhereScope() }
) : FilterScope by support,
    List<Criterion> by support {

    companion object {
        operator fun WhereDeclaration.plus(other: WhereDeclaration): WhereDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

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
