package org.komapper.core

import kotlin.reflect.KClass

@ThreadSafe
data class Statement(val charSequences: List<CharSequence>, val args: List<Value>) {
    constructor(charSequence: CharSequence) : this(listOf(charSequence), emptyList())

    companion object {
        val EMPTY = Statement(emptyList(), emptyList())
    }

    init {
        val count = charSequences.count { it is PlaceHolder }
        val size = args.size
        require(count == args.size) { "count=$count, size=$size" }
    }

    fun toSql(transform: (Int, PlaceHolder) -> CharSequence = { _, placeHolder -> placeHolder }): String {
        var index = 0
        return charSequences.joinToString(separator = "") { each ->
            when (each) {
                is PlaceHolder -> {
                    transform(index++, each)
                }
                else -> each
            }
        }
    }

    fun toSqlWithArgs(format: (Any?, KClass<*>) -> String): String {
        val iterator = args.iterator()
        var index = 0
        return charSequences.joinToString(separator = "") { each ->
            when (each) {
                is PlaceHolder -> {
                    if (!iterator.hasNext()) error("The value is not found. index=$index")
                    val value = iterator.next()
                    index++
                    format(value.any, value.klass)
                }
                else -> each
            }
        }
    }

    infix operator fun plus(other: Statement): Statement {
        val separator =
            if (this.charSequences.isEmpty() || this.charSequences.last().trimEnd().endsWith(";")) "" else ";"
        val charSequences = this.charSequences + separator + other.charSequences
        val args = this.args + other.args
        return Statement(charSequences, args)
    }
}
