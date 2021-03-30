package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.EntityUpdateOptions
import org.komapper.core.config.OptionsImpl

@Scope
class EntityUpdateOptionsScope internal constructor(
    internal var options: EntityUpdateOptions
) {

    companion object {
        operator fun EntityUpdateOptionsDeclaration.plus(other: EntityUpdateOptionsDeclaration): EntityUpdateOptionsDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var queryTimeoutSeconds: Int?
        get() = options.queryTimeoutSeconds
        set(value) {
            options = OptionsImpl(queryTimeoutSeconds = value)
        }
}
