package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.options.ScriptOptions

@ThreadSafe
data class ScriptContext(
    val sql: String = "",
    val options: ScriptOptions = ScriptOptions.DEFAULT
)
