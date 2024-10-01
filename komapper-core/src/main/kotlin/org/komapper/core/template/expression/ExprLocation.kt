package org.komapper.core.template.expression

class ExprLocation(
    val expression: String,
    @Deprecated("Do not use this property directly.")
    val position: Int,
    val lineNumber: Int = -1,
    val startIndex: Int = -1,
) {
    override fun toString(): String = "[$expression]:$lineNumber:${startIndex + 1}"
}
