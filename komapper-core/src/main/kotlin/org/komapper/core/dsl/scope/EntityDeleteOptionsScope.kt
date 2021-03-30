package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.EntityDeleteOptions
import org.komapper.core.config.OptionsImpl

@Scope
class EntityDeleteOptionsScope internal constructor(
    internal var options: EntityDeleteOptions
) {

    companion object {
        operator fun EntityDeleteOptionsDeclaration.plus(other: EntityDeleteOptionsDeclaration): EntityDeleteOptionsDeclaration {
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
