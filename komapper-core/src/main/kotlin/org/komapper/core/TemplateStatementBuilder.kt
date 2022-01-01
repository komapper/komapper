package org.komapper.core

/**
 * The builder for creating a [Statement] from a template.
 */
@ThreadSafe
interface TemplateStatementBuilder {
    /**
     * Builds a [Statement].
     * @param template the template
     * @param data the data for the template
     * @param escape the escape function for LIKE predicate
     * @return the [Statement]
     */
    fun build(
        template: CharSequence,
        data: Any,
        escape: (String) -> String
    ): Statement

    /**
     * Clears the cache.
     */
    fun clearCache()
}
