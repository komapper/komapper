package org.komapper.template.expression

import java.util.Deque

internal abstract class ExprReducer(val priority: Int, val location: ExprLocation) {
    abstract fun reduce(deque: Deque<ExprNode>): ExprNode

    fun pop(deque: Deque<ExprNode>): ExprNode =
        deque.poll() ?: throw ExprException("The operand is not found at $location")
}

internal class PropertyReducer(location: ExprLocation, val name: String, private val safeCall: Boolean) :
    ExprReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val receiver = pop(deque)
        return ExprNode.Property(location, name, safeCall, receiver)
    }
}

internal class FunctionReducer(location: ExprLocation, val name: String, private val safeCall: Boolean) :
    ExprReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val args = pop(deque)
        val receiver = pop(deque)
        return ExprNode.Function(location, name, safeCall, receiver, args)
    }
}

internal class NotReducer(location: ExprLocation) : ExprReducer(50, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val expr = pop(deque)
        return ExprNode.Not(location, expr)
    }
}

internal class EqReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Eq(location, left, right)
    }
}

internal class NeReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Ne(location, left, right)
    }
}

internal class GeReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Ge(location, left, right)
    }
}

internal class GtReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Gt(location, left, right)
    }
}

internal class LeReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Le(location, left, right)
    }
}

internal class LtReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Lt(location, left, right)
    }
}

internal class AndReducer(location: ExprLocation) : ExprReducer(20, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.And(location, left, right)
    }
}

internal class OrReducer(location: ExprLocation) : ExprReducer(10, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return ExprNode.Or(location, left, right)
    }
}

internal class CommaReducer(location: ExprLocation) : ExprReducer(0, location) {
    @Suppress("MoveVariableDeclarationIntoWhen")
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        val exprList = when (left) {
            is ExprNode.Comma -> left.nodeList + right
            else -> listOf(left, right)
        }
        return ExprNode.Comma(location, exprList)
    }
}
