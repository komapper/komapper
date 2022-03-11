package org.komapper.template.expression

import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.Value

internal class ExprContext(
    val valueMap: Map<String, Value<*>>,
    val builtinExtensions: TemplateBuiltinExtensions
)
