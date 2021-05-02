package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Criterion

@Scope
class WhenScope internal constructor(
    private val support: FilterScopeSupport = FilterScopeSupport()
) : FilterScope by support,
    List<Criterion> by support {

    companion object {
        operator fun WhenDeclaration.plus(other: WhenDeclaration): WhenDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

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
        val childScope = WhenScope()
        declaration(childScope)
        val criteria = childScope.toList()
        support.add(operator(criteria))
    }
}
