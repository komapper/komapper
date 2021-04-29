package org.komapper.core

interface TemplateStatementBuilder {
    fun build(
        template: CharSequence,
        params: Any,
        escape: (String) -> String
    ): Statement

    fun clearCache()
}
