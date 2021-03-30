package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.SqlInsertOptions

@Scope
class SqlInsertOptionsScope internal constructor(
    internal var options: SqlInsertOptions
) {

    companion object {
        operator fun SqlInsertOptionsDeclaration.plus(other: SqlInsertOptionsDeclaration): SqlInsertOptionsDeclaration {
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
