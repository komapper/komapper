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
     * @param builtinExtensions the builtin extensions
     * @return the [Statement]
     */
    fun build(
        template: CharSequence,
        valueMap: Map<String, Value<*>>,
        builtinExtensions: TemplateBuiltinExtensions
    ): Statement

    /**
     * Clears the cache.
     */
    fun clearCache()
}

class DefaultTemplateStatementBuilder : TemplateStatementBuilder {
    override fun build(template: CharSequence, valueMap: Map<String, Value<*>>, builtinExtensions: TemplateBuiltinExtensions): Statement {
        throw UnsupportedOperationException(
            "Appropriate TemplateStatementBuilder is not found. " +
                "Add komapper-template dependency or override the templateStatementBuilder property."
        )
    }

    override fun clearCache() {
    }
}
