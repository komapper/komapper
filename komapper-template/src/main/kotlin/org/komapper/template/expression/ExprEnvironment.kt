package org.komapper.template.expression

import org.komapper.core.Value
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

internal interface ExprEnvironment {
    val ctx: Map<String, Value<*>>
    val topLevelPropertyExtensions: List<KProperty<*>>
    val topLevelFunctionExtensions: List<KFunction<*>>
}

internal class DefaultExprEnvironment(override val ctx: Map<String, Value<*>> = emptyMap()) : ExprEnvironment {

    override val topLevelPropertyExtensions: List<KProperty<*>> by lazy {
        listOf(
            CharSequence::lastIndex
        ).onEach { it.isAccessible = true }
    }

    override val topLevelFunctionExtensions: List<KFunction<*>> by lazy {
        listOf(
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
}
