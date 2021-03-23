package org.komapper.core.query.scope

private const val DEFAULT_ESCAPE_CHAR: Char = '\\'

sealed class LikeOperand {
    abstract val value: Any?
    data class Normal(override val value: Any?) : LikeOperand()
    data class Escape(override val value: Any?, val escapeChar: Char = DEFAULT_ESCAPE_CHAR) : LikeOperand()
    data class Prefix(override val value: Any?, val escapeChar: Char = DEFAULT_ESCAPE_CHAR) : LikeOperand()
    data class Infix(override val value: Any?, val escapeChar: Char = DEFAULT_ESCAPE_CHAR) : LikeOperand()
    data class Suffix(override val value: Any?, val escapeChar: Char = DEFAULT_ESCAPE_CHAR) : LikeOperand()
}
