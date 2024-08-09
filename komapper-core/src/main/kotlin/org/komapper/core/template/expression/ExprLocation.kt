package org.komapper.template.expression

class ExprLocation(val expression: String, val position: Int) {
    override fun toString(): String = "<$expression>:$position"
}
