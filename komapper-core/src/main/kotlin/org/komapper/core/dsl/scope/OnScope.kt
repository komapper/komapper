package org.komapper.core.dsl.scope

import org.komapper.core.Scope

/**
 * Provides operators for the ON clause.
 */
@Scope
class OnScope(
    private val support: FilterScopeSupport<OnScope>,
) : FilterScope<OnScope> by support
