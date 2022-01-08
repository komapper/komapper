package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.options.TemplateExecuteOptions

@ThreadSafe
data class TemplateExecuteContext(
    val sql: String,
    val data: Any = object {},
    val options: TemplateExecuteOptions = TemplateExecuteOptions.DEFAULT
)
