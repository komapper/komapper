package org.komapper.core.dsl.context

import org.komapper.core.dsl.options.TemplateExecuteOptions

data class TemplateExecuteContext(
    val sql: String,
    val data: Any = object {},
    val options: TemplateExecuteOptions = TemplateExecuteOptions.default
)
