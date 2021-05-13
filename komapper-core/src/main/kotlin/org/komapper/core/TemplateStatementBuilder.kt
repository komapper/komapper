package org.komapper.core

@ThreadSafe
interface TemplateStatementBuilder {
    fun build(
        template: CharSequence,
        params: Any,
        escape: (String) -> String
    ): Statement

    fun clearCache()
}
