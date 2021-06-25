package org.komapper.core

import kotlin.reflect.KClass

@ThreadSafe
data class Statement(val charSequences: List<CharSequence>, val args: List<Value>) {
    constructor(charSequence: CharSequence) : this(listOf(charSequence), emptyList())

    companion object {
        val EMPTY = Statement(emptyList(), emptyList())
    }

    fun toSql(transform: (Int, PlaceHolder) -> CharSequence = { _, placeHolder -> placeHolder }): String {
        var index = 0
        return charSequences.joinToString(separator = "") { fragment ->
            when (fragment) {
                is PlaceHolder -> {
                    transform(index++, fragment)
                }
                else -> fragment
            }
        }
    }

    fun toSqlWithArgs(format: (Any?, KClass<*>) -> String): String {
        val iterator = args.iterator()
        var index = 0
        return charSequences.joinToString(separator = "") { fragment ->
            when (fragment) {
                is PlaceHolder -> {
                    if (!iterator.hasNext()) error("The value is not found. index=$index")
                    val value = iterator.next()
                    index++
                    format(value.any, value.klass)
                }
                else -> fragment
            }
        }
    }

    infix operator fun plus(other: Statement): Statement {
        val separator = if (this.charSequences.isEmpty() || this.charSequences.last().trimEnd().endsWith(";")) "" else ";"
        val newFragments = this.charSequences + listOf(separator) + other.charSequences
        val newArgs = this.args + other.args
        return Statement(newFragments, newArgs)
    }
}
