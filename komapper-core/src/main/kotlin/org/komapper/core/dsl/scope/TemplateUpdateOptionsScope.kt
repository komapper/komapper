package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.TemplateUpdateOptions

@Scope
class TemplateUpdateOptionsScope internal constructor(
    internal var options: TemplateUpdateOptions
) {

    companion object {
        operator fun TemplateUpdateOptionsDeclaration.plus(other: TemplateUpdateOptionsDeclaration): TemplateUpdateOptionsDeclaration {
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
