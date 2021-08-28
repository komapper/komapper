package org.komapper.core

@ThreadSafe
interface TemplateStatementBuilder {
    fun build(
        template: CharSequence,
        data: Any,
        escape: (String) -> String
    ): Statement

    fun clearCache()
}
