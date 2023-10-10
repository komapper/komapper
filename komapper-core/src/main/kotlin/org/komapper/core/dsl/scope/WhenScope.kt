package org.komapper.core.dsl.scope

import org.komapper.core.Scope

/**
 * Provides operators for the WHEN clause.
 */
@Scope
class WhenScope(
    private val support: FilterScopeSupport<WhenScope>,
) : FilterScope<WhenScope> by support
