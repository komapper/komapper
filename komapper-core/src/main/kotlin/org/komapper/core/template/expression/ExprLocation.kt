package org.komapper.core.template.expression

class ExprLocation(val expression: String, val position: Int) {
    override fun toString(): String = "<$expression>:$position"
}
