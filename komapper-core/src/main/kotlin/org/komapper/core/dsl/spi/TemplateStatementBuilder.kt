package org.komapper.core.dsl.spi

import org.komapper.core.Dialect
import org.komapper.core.data.Statement

interface TemplateStatementBuilder {
    fun build(
        template: CharSequence,
        params: Any,
        escape: (String) -> String
    ): Statement
}

interface TemplateStatementBuilderFactory {
    fun create(dialect: Dialect, cache: Boolean = false): TemplateStatementBuilder
}
