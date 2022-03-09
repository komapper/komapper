package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.Value
import org.komapper.core.dsl.options.TemplateExecuteOptions

@ThreadSafe
data class TemplateExecuteContext(
    val sql: String,
    val valueMap: Map<String, Value<*>> = emptyMap(),
    val options: TemplateExecuteOptions = TemplateExecuteOptions.DEFAULT
)
