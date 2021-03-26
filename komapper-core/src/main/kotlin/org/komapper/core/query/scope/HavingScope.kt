package org.komapper.core.query.scope

import org.komapper.core.Scope
import org.komapper.core.query.context.FilterContext
import org.komapper.core.query.data.Criterion

@Scope
class HavingScope internal constructor(
    private val support: FilterScopeSupport
) : FilterScope by support {

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
        val criteria = mutableListOf<Criterion>()
        val subContext = FilterContext(criteria)
        val support = FilterScopeSupport(subContext)
        val scope = HavingScope(support)
        declaration(scope)
        support.add(operator(criteria))
    }
}
