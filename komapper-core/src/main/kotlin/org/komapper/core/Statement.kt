package org.komapper.core

import kotlin.reflect.KClass

@ThreadSafe
data class Statement(val fragments: List<CharSequence>, val args: List<Value>) {
    constructor(fragment: CharSequence) : this(listOf(fragment), emptyList())

    companion object {
        val EMPTY = Statement(emptyList(), emptyList())
    }

    fun toSql(transform: (Int, PlaceHolder) -> CharSequence = { _, placeHolder -> placeHolder }): String {
        var index = 0
        return fragments.joinToString(separator = "") { fragment ->
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
        return fragments.joinToString(separator = "") { fragment ->
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
        val separator = if (this.fragments.isEmpty() || this.fragments.last().trimEnd().endsWith(";")) "" else ";"
        val newFragments = this.fragments + listOf(separator) + other.fragments
        val newArgs = this.args + other.args
        return Statement(newFragments, newArgs)
    }
}
