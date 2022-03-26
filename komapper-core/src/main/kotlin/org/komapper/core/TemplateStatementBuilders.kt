package org.komapper.core

import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.core.spi.findByPriority
import java.util.ServiceLoader

/**
 * The provider of [TemplateStatementBuilder].
 */
object TemplateStatementBuilders {
    /**
     * @param dialect the builder dialect
     * @return the [TemplateStatementBuilder] instance
     */
    fun get(dialect: BuilderDialect): TemplateStatementBuilder {
        val loader = ServiceLoader.load(TemplateStatementBuilderFactory::class.java)
        val factory = loader.findByPriority()
        return factory?.create(dialect) ?: DefaultTemplateStatementBuilder()
    }
}
