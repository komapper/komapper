package org.komapper.core.template.expr

class ExprLocation(val expression: String, val position: Int) {
    override fun toString(): String = "<$expression>:$position"
}
