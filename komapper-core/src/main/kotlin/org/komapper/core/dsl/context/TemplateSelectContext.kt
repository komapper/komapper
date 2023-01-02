package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.Value
import org.komapper.core.dsl.options.TemplateSelectOptions

@ThreadSafe
data class TemplateSelectContext(
    val sql: String,
    val valueMap: Map<String, Value<*>> = emptyMap(),
    val options: TemplateSelectOptions = TemplateSelectOptions.DEFAULT,
)
