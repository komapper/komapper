package org.komapper.core.spi

import org.komapper.core.BuilderDialect
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.ThreadSafe

/**
 * The factory of [TemplateStatementBuilder].
 */
@ThreadSafe
interface TemplateStatementBuilderFactory : Prioritized {
    /**
     * Create a [TemplateStatementBuilder].
     *
     * @param dialect the builder dialect
     * @param enableCache whether to enable cache or not
     * @return the [TemplateStatementBuilder].
     */
    fun create(dialect: BuilderDialect, enableCache: Boolean = false): TemplateStatementBuilder
}
