package org.komapper.core.spi

import org.komapper.core.Dialect
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
     * @param dialect the dialect of database
     * @param enableCache whether to enable cache or not
     * @return the [TemplateStatementBuilder].
     */
    fun create(dialect: Dialect, enableCache: Boolean = false): TemplateStatementBuilder
}
