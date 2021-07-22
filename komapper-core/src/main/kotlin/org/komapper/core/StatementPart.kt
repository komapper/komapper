package org.komapper.core

sealed class StatementPart : CharSequence {
    data class Text(val text: CharSequence) : StatementPart(), CharSequence by text
    data class PlaceHolder(val value: Value) : StatementPart(), CharSequence by "?"
}
