package org.komapper.template.expression

import org.komapper.core.template.expression.ExprLocation
import java.util.Deque

abstract class ExprReducer(val priority: Int, val location: ExprLocation) {
    abstract fun reduce(deque: Deque<ExprNode>): ExprNode

    fun pop(deque: Deque<ExprNode>): ExprNode =
        deque.poll() ?: throw ExprException("The operand is not found at $location")
}

class PropertyReducer(location: ExprLocation, val name: String, private val safeCall: Boolean) :
    ExprReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val receiver = pop(deque)
        return ExprNode.Property(location, name, safeCall, receiver)
    }
}

class FunctionReducer(location: ExprLocation, val name: String, private val safeCall: Boolean) :
    ExprReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val args = pop(deque)
        val receiver = pop(deque)
        return ExprNode.Function(location, name, safeCall, receiver, args)
    }
}

class NotReducer(location: ExprLocation) : ExprReducer(50, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val expr = pop(deque)
        return ExprNode.Not(location, expr)
    }
}

class EqReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Eq(location, left, right)
    }
}

class NeReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Ne(location, left, right)
    }
}

class GeReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Ge(location, left, right)
    }
}

class GtReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Gt(location, left, right)
    }
}

class LeReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Le(location, left, right)
    }
}

class LtReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Lt(location, left, right)
    }
}

class AndReducer(location: ExprLocation) : ExprReducer(20, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.And(location, left, right)
    }
}

class OrReducer(location: ExprLocation) : ExprReducer(10, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Or(location, left, right)
    }
}

class CommaReducer(location: ExprLocation) : ExprReducer(0, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        val exprList = when (right) {
            is ExprNode.Comma -> listOf(left) + right.nodeList
            else -> listOf(left, right)
        }
        return ExprNode.Comma(location, exprList)
    }
}
