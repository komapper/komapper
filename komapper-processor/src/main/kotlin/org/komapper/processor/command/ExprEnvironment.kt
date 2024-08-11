package org.komapper.processor.command

import org.komapper.core.Value
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

internal interface ExprEnvironment {
    val ctx: Map<String, Value<*>>
    val topLevelPropertyExtensions: List<KProperty<*>>
    val topLevelFunctionExtensions: List<KFunction<*>>
}
