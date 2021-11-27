package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.options.TemplateSelectOptions

@ThreadSafe
data class TemplateSelectContext(
    val sql: String,
    val data: Any = object {},
    val options: TemplateSelectOptions = TemplateSelectOptions.default
)
