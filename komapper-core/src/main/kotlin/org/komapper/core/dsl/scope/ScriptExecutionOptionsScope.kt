package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.ScriptExecutionOptions

@Scope
class ScriptExecutionOptionsScope internal constructor(
    internal var options: ScriptExecutionOptions
) {

    companion object {
        operator fun ScriptExecutionOptionsDeclaration.plus(other: ScriptExecutionOptionsDeclaration): ScriptExecutionOptionsDeclaration {
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
