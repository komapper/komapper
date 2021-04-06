package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Criterion

@Scope
class HavingScope internal constructor(
    private val support: FilterScopeSupport = FilterScopeSupport()
) : FilterScope by support,
    Collection<Criterion> by support {

    companion object {
        operator fun HavingDeclaration.plus(other: HavingDeclaration): HavingDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

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
        val childScope = HavingScope()
        declaration(childScope)
        val criteria = childScope.toList()
        support.add(operator(criteria))
    }
}
