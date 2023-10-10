package org.komapper.core.dsl.scope

import org.komapper.core.Scope

/**
 * Provides operators for the HAVING clause.
 */
@Scope
class HavingScope(
    private val support: FilterScopeSupport<HavingScope>,
) : FilterScope<HavingScope> by support
