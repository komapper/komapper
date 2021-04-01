package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.QueryOptionImpl
import org.komapper.core.dsl.query.ScriptExecutionOption

@Scope
class ScriptExecutionOptionScope internal constructor(
    internal var option: ScriptExecutionOption
) {

    companion object {
        operator fun ScriptExecutionOptionDeclaration.plus(other: ScriptExecutionOptionDeclaration): ScriptExecutionOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var queryTimeoutSeconds: Int?
        get() = option.queryTimeoutSeconds
        set(value) {
            option = QueryOptionImpl(queryTimeoutSeconds = value)
        }
}
