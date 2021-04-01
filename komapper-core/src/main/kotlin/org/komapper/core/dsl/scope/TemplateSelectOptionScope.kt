package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.query.QueryOptionImpl
import org.komapper.core.dsl.query.TemplateSelectOption

@Scope
class TemplateSelectOptionScope internal constructor(
    option: TemplateSelectOption
) {

    companion object {
        operator fun TemplateSelectOptionDeclaration.plus(other: TemplateSelectOptionDeclaration): TemplateSelectOptionDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    var fetchSize = option.fetchSize
    var maxRows = option.maxRows
    var queryTimeoutSeconds = option.queryTimeoutSeconds

    internal fun asOption(): TemplateSelectOption = QueryOptionImpl(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds,
    )
}
