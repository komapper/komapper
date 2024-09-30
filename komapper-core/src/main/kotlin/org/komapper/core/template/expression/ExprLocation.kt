package org.komapper.core.template.expression

class ExprLocation(val expression: String, val startIndex: Int, val endIndex: Int) {
    override fun toString(): String = "[$expression]:$startIndex..$endIndex"
}
