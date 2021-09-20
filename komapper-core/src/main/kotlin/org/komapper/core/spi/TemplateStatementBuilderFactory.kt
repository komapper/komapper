package org.komapper.core.spi

import org.komapper.core.Dialect
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.ThreadSafe
import java.util.ServiceLoader

@ThreadSafe
interface TemplateStatementBuilderFactory {
    fun create(dialect: Dialect, enableCache: Boolean = false): TemplateStatementBuilder
}

object TemplateStatementBuilderProvider {
    fun get(dialect: Dialect): TemplateStatementBuilder {
        val loader = ServiceLoader.load(TemplateStatementBuilderFactory::class.java)
        val factory = loader.firstOrNull()
            ?: error(
                "TemplateStatementBuilderFactory is not found. " +
                    "Add komapper-template dependency or override the templateStatementBuilder property."
            )
        return factory.create(dialect)
    }
}
