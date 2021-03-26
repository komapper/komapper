package org.komapper.core.query.scope

import org.komapper.core.Scope

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
}
