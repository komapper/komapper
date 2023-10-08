package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.SqlBuilderScope
import org.komapper.core.dsl.scope.FilterScope

/**
 * The context for criteria.
 */
interface CriteriaContext {
    /**
     * The parent scope.
     */
    val scope: FilterScope

    /**
     * Adds a SQL builder
     * @param build the SQL builder
     */
    fun add(build: SqlBuilderScope.() -> Unit)
}

class CriteriaContextImpl(
    override val scope: FilterScope,
    val
    criteria: MutableList<Criterion>,
) : CriteriaContext {
    override fun add(build: SqlBuilderScope.() -> Unit) {
        val criterion = Criterion.UserDefined(build)
        criteria.add(criterion)
    }
}
