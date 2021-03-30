package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.EntityInsertOptions
import org.komapper.core.config.OptionsImpl

@Scope
class EntityInsertOptionsScope internal constructor(
    internal var options: EntityInsertOptions
) {

    companion object {
        operator fun EntityInsertOptionsDeclaration.plus(other: EntityInsertOptionsDeclaration): EntityInsertOptionsDeclaration {
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
