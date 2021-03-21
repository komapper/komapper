package org.komapper.core.expr

class ExprLocation(val expression: String, val position: Int) {
    override fun toString(): String = "<$expression>:$position"
}
