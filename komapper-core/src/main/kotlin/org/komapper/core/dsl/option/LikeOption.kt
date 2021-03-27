package org.komapper.core.dsl.option

internal sealed class LikeOption {
    object None : LikeOption()
    data class Escape(val escapeChar: Char) : LikeOption()
    data class Prefix(val escapeChar: Char) : LikeOption()
    data class Infix(val escapeChar: Char) : LikeOption()
    data class Suffix(val escapeChar: Char) : LikeOption()
}
