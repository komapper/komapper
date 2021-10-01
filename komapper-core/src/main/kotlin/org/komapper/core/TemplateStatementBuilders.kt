package org.komapper.core

import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.core.spi.findByPriority
import java.util.ServiceLoader

object TemplateStatementBuilders {
    fun get(dialect: Dialect): TemplateStatementBuilder {
        val loader = ServiceLoader.load(TemplateStatementBuilderFactory::class.java)
        val factory = loader.findByPriority()
            ?: error(
                "TemplateStatementBuilderFactory is not found. " +
                    "Add komapper-template dependency or override the templateStatementBuilder property."
            )
        return factory.create(dialect)
    }
}
