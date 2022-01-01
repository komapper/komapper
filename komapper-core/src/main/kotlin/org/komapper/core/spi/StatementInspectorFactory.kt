package org.komapper.core.spi

import org.komapper.core.StatementInspector
import org.komapper.core.ThreadSafe

/**
 * The factory of [StatementInspector].
 */
@ThreadSafe
interface StatementInspectorFactory : Prioritized {
    fun create(): StatementInspector
}
