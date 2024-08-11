package org.komapper.template

import org.komapper.core.Value
import kotlin.collections.onEach
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible
import kotlin.text.any
import kotlin.text.isBlank
import kotlin.text.isEmpty
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty
import kotlin.text.isNullOrBlank
import kotlin.text.isNullOrEmpty
import kotlin.text.lastIndex
import kotlin.text.none

@Deprecated("Use org.komapper.core.TemplateBuiltinExtensions")
internal interface ExprEnvironment {
    val ctx: Map<String, Value<*>>
    val topLevelPropertyExtensions: List<KProperty<*>>
    val topLevelFunctionExtensions: List<KFunction<*>>
}

@Suppress("DEPRECATION")
internal class DefaultExprEnvironment(override val ctx: Map<String, Value<*>> = emptyMap()) : ExprEnvironment {

    override val topLevelPropertyExtensions: List<KProperty<*>> by lazy {
        listOf(
            CharSequence::lastIndex,
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
            CharSequence::none,
        ).onEach { it.isAccessible = true }
    }
}
