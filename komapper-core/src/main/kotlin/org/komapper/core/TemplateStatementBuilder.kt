package org.komapper.core

/**
 * The builder for creating a [Statement] from a template.
 */
@ThreadSafe
interface TemplateStatementBuilder {
    /**
     * Builds a [Statement].
     * @param template the template
     * @param valueMap the data for the template
     * @param escape the escape function for LIKE predicate
     * @return the [Statement]
     */
    fun build(
        template: CharSequence,
        valueMap: Map<String, Value<*>>,
        escape: (String) -> String
    ): Statement

    /**
     * Clears the cache.
     */
    fun clearCache()
}

class DefaultTemplateStatementBuilder : TemplateStatementBuilder {
    override fun build(template: CharSequence, valueMap: Map<String, Value<*>>, escape: (String) -> String): Statement {
        throw UnsupportedOperationException(
            "Appropriate TemplateStatementBuilder is not found. " +
                "Add komapper-template dependency or override the templateStatementBuilder property."
        )
    }

    override fun clearCache() {
    }
}
