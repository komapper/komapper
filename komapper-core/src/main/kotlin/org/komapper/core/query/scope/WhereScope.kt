package org.komapper.core.query.scope

import org.komapper.core.Scope
import org.komapper.core.query.data.Criterion

@Scope
class WhereScope internal constructor(
    private val support: FilterScopeSupport = FilterScopeSupport()
) : FilterScope by support {

    companion object {
        operator fun WhereDeclaration.plus(other: WhereDeclaration): WhereDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    internal val criteria: List<Criterion> get() = support.context.criteria

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
        val childScope = WhereScope()
        declaration(childScope)
        val criteria = childScope.criteria.toList()
        support.add(operator(criteria))
    }
}
