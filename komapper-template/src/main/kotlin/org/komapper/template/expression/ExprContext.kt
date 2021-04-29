package org.komapper.template.expression

import org.komapper.core.Value

internal class ExprContext(val valueMap: Map<String, Value>, val functionExtensions: ExprBuiltinFunctionExtensions) {
    constructor(
        valueMap: Map<String, Value> = emptyMap(),
        escape: (String) -> String = { it }
    ) : this(valueMap, ExprBuiltinFunctionExtensions(escape))
}
