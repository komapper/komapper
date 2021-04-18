package org.komapper.template.expression

import org.komapper.core.data.Value
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

interface ExprEnvironment {
    val ctx: Map<String, Value>
    val topLevelPropertyExtensions: List<KProperty<*>>
    val topLevelFunctionExtensions: List<KFunction<*>>
}

open class DefaultExprEnvironment(override val ctx: Map<String, Value> = emptyMap()) : ExprEnvironment {

    override val topLevelPropertyExtensions: List<KProperty<*>> = listOf(
        CharSequence::lastIndex
    ).onEach { it.isAccessible = true }

    override val topLevelFunctionExtensions: List<KFunction<*>> = listOf(
        CharSequence::isBlank,
        CharSequence::isNotBlank,
        CharSequence::isNullOrBlank,
        CharSequence::isEmpty,
        CharSequence::isNotEmpty,
        CharSequence::isNullOrEmpty,
        CharSequence::any,
        CharSequence::none
    ).onEach { it.isAccessible = true }
}
