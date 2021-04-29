package org.komapper.template.expression

internal class ExprLocation(val expression: String, val position: Int) {
    override fun toString(): String = "<$expression>:$position"
}
