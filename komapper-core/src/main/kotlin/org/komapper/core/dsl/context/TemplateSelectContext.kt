package org.komapper.core.dsl.context

import org.komapper.core.dsl.options.TemplateSelectOptions

data class TemplateSelectContext(
    val sql: String,
    val data: Any = object {},
    val options: TemplateSelectOptions = TemplateSelectOptions.default
)
