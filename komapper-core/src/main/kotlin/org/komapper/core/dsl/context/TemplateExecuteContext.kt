package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.Value
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.options.TemplateSelectOptions

@ThreadSafe
data class TemplateExecuteContext(
    val sql: String,
    val valueMap: Map<String, Value<*>> = emptyMap(),
    val options: TemplateExecuteOptions = TemplateExecuteOptions.DEFAULT,
) {
    fun asTemplateSelectContext(returning: Boolean): TemplateSelectContext {
        return TemplateSelectContext(
            sql = sql,
            valueMap = valueMap,
            options = TemplateSelectOptions(
                escapeSequence = options.escapeSequence,
                queryTimeoutSeconds = options.queryTimeoutSeconds,
                suppressLogging = options.suppressLogging,
            ),
            returning = returning,
        )
    }
}
