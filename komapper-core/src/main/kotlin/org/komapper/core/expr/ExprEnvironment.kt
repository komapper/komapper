package org.komapper.core.expr

import org.komapper.core.data.Value
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

interface ExprEnvironment {
    val ctx: Map<String, Value>
    val topLevelPropertyExtensions: List<KProperty<*>>
    val topLevelFunctionExtensions: List<KFunction<*>>
}

open class DefaultExprEnvironment(val escape: (String) -> String) : ExprEnvironment {

    override val ctx: Map<String, Value> = emptyMap()

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

    open fun String?.escape(): String? {
        return this?.let { escape(it) }
    }

    open fun String?.asPrefix(): String? {
        return this?.let { "${escape(it)}%" }
    }

    open fun String?.asInfix(): String? {
        return this?.let { "%${escape(it)}%" }
    }

    open fun String?.asSuffix(): String? {
        return this?.let { "%${escape(it)}" }
    }
}
