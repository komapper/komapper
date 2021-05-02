package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Criterion

@Scope
class HavingScope internal constructor(
    private val support: FilterScopeSupport<HavingScope> = FilterScopeSupport { HavingScope() }
) : FilterScope by support,
    List<Criterion> by support {

    companion object {
        operator fun HavingDeclaration.plus(other: HavingDeclaration): HavingDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

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
