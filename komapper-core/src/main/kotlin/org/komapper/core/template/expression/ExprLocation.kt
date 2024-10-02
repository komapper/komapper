package org.komapper.core.template.expression

class ExprLocation(
    val expression: String,
    @Deprecated("Do not use this property directly.")
    val position: Int,
    val lineNumber: Int,
    val columnNumber: Int,
) {
    override fun toString(): String = "[$expression]:$lineNumber:$columnNumber"
}
