package org.komapper.core.query.scope

import org.komapper.core.Scope
import org.komapper.core.query.context.FilterContext
import org.komapper.core.query.data.Criterion

@Scope
class WhereScope internal constructor(
    private val support: FilterScopeSupport
) : FilterScope by support {

    companion object {
        operator fun WhereDeclaration.plus(other: WhereDeclaration): WhereDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

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
        val criteria = mutableListOf<Criterion>()
        val subContext = FilterContext(criteria)
        val subSupport = FilterScopeSupport(subContext)
        val scope = WhereScope(subSupport)
        declaration(scope)
        support.add(operator(criteria))
    }
}
