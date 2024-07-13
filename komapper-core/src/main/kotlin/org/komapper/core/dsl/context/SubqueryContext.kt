package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.options.SelectOptions

@ThreadSafe
sealed interface SubqueryContext {
    val options: SelectOptions
}
