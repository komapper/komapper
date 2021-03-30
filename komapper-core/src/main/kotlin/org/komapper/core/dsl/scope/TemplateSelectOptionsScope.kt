package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.TemplateSelectOptions

@Scope
class TemplateSelectOptionsScope internal constructor(
    internal var options: TemplateSelectOptions
) {

    companion object {
        operator fun TemplateSelectOptionsDeclaration.plus(other: TemplateSelectOptionsDeclaration): TemplateSelectOptionsDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var fetchSize: Int?
        get() = options.fetchSize
        set(value) {
            options = OptionsImpl(fetchSize = value)
        }

    var maxRows: Int?
        get() = options.maxRows
        set(value) {
            options = OptionsImpl(maxRows = value)
        }

    var queryTimeoutSeconds: Int?
        get() = options.queryTimeoutSeconds
        set(value) {
            options = OptionsImpl(queryTimeoutSeconds = value)
        }
}
