package org.komapper.core.dsl.context

import org.komapper.core.dsl.options.ScriptOptions

data class ScriptContext(
    val sql: String = "",
    val options: ScriptOptions = ScriptOptions.default
)
