package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.options.ForUpdateOptions

@ThreadSafe
data class ForUpdate(
    val options: ForUpdateOptions? = null
)
