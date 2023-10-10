package org.komapper.core.dsl.scope

import org.komapper.core.Scope

/**
 * Provides operators for the WHERE clause.
 */
@Scope
class WhereScope(
    private val support: FilterScopeSupport<WhereScope>,
) : FilterScope<WhereScope> by support
