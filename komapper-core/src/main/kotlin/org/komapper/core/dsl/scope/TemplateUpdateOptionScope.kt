package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.QueryOptionImpl
import org.komapper.core.dsl.query.TemplateUpdateOption

@Scope
class TemplateUpdateOptionScope internal constructor(
    option: TemplateUpdateOption
) {

    companion object {
        operator fun TemplateUpdateOptionDeclaration.plus(other: TemplateUpdateOptionDeclaration): TemplateUpdateOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var queryTimeoutSeconds = option.queryTimeoutSeconds

    internal fun asOption(): TemplateUpdateOption = QueryOptionImpl(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}
