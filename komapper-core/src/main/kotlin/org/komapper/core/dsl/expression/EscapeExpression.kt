package org.komapper.core.dsl.expression

sealed class EscapeExpression : CharSequence {
    internal data class Text(val value: CharSequence) : EscapeExpression(), CharSequence by value
    internal data class Escape(val value: CharSequence) :
        EscapeExpression(),
        CharSequence by value

    internal data class Composite(val left: EscapeExpression, val right: EscapeExpression) :
        EscapeExpression(),
        CharSequence {
        override val length: Int
            get() = left.length + right.length

        override fun get(index: Int): Char {
            val string = left.toString() + right.toString()
            return string[index]
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            val string = left.toString() + right.toString()
            return string.subSequence(startIndex, endIndex)
        }
    }

    infix operator fun plus(other: EscapeExpression): EscapeExpression {
        return Composite(this, other)
    }
}
