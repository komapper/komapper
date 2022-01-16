package org.komapper.core

import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.core.spi.findByPriority
import java.util.ServiceLoader

/**
 * The provider of [TemplateStatementBuilder].
 */
object TemplateStatementBuilders {
    /**
     * @param dialect the dialect of the database
     * @return the [TemplateStatementBuilder] instance
     */
    fun get(dialect: Dialect): TemplateStatementBuilder {
        val loader = ServiceLoader.load(TemplateStatementBuilderFactory::class.java)
        val factory = loader.findByPriority()
        return factory?.create(dialect) ?: DefaultTemplateStatementBuilder()
    }
}
