package org.komapper.core.template.sql

import java.util.*

internal abstract class SqlReducer {
    protected val nodeList = LinkedList<SqlNode>()
    abstract fun reduce(): SqlNode
    open fun addNode(node: SqlNode) = nodeList.add(node)
}

internal class SetReducer(private val location: SqlLocation, private val keyword: String, private val left: SqlNode) :
    SqlReducer() {
    override fun reduce(): SqlNode {
        val right = nodeList.poll() ?: throw SqlException("The right node is not found.")
        return SqlNode.Set(location, keyword, left, right)
    }
}

internal class StatementReducer : SqlReducer() {
    override fun reduce(): SqlNode = SqlNode.Statement(nodeList)
}

internal class SelectReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.Clause.Select(location, keyword, nodeList)
}

internal class FromReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.Clause.From(location, keyword, nodeList)
}

internal class WhereReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.Clause.Where(location, keyword, nodeList)
}

internal class HavingReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.Clause.Having(location, keyword, nodeList)
}

internal class GroupByReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.Clause.GroupBy(location, keyword, nodeList)
}

internal class OrderByReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.Clause.OrderBy(location, keyword, nodeList)
}

internal class ForUpdateReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.Clause.ForUpdate(location, keyword, nodeList)
}

internal class OptionReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.Clause.Option(location, keyword, nodeList)
}

internal class AndReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.BiLogicalOp.And(location, keyword, nodeList)
}

internal class OrReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.BiLogicalOp.Or(location, keyword, nodeList)
}

internal class BindValueDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val expression: String,
) :
    SqlReducer() {
    override fun reduce(): SqlNode = when (val node = nodeList.poll()) {
        is SqlNode.Token.Word, is SqlNode.Paren -> SqlNode.BindValueDirective(
            location,
            token,
            expression,
            node,
            nodeList,
        )

        else -> throw SqlException("The test value must follow the bind value directive at $location. node=$node")
    }
}

internal class LiteralValueDirectiveReducer(
    private val location: SqlLocation,
    val token: String,
    private val expression: String,
) : SqlReducer() {
    override fun reduce(): SqlNode = when (val node = nodeList.poll()) {
        is SqlNode.Token.Word -> SqlNode.LiteralValueDirective(
            location,
            token,
            expression,
            node,
            nodeList,
        )

        else -> throw SqlException("The test value must follow the literal value directive at $location")
    }
}

internal abstract class BlockReducer : SqlReducer()

internal class IfBlockReducer(private val location: SqlLocation) : BlockReducer() {

    override fun addNode(node: SqlNode) = when (node) {
        is SqlNode.IfDirective,
        is SqlNode.ElseifDirective,
        is SqlNode.ElseDirective,
        is SqlNode.EndDirective,
        -> super.addNode(node)

        else -> error(node)
    }

    override fun reduce(): SqlNode {
        val ifDirective = getIfDirective()
        val elseifDirectives = getElseifDirectives()
        val elseDirective = getElseDirective()
        val endDirect = getEndDirective()
        return SqlNode.IfBlock(ifDirective, elseifDirectives, elseDirective, endDirect)
    }

    private fun getIfDirective(): SqlNode.IfDirective {
        val node = nodeList.poll() as? SqlNode.IfDirective
        return node ?: error("IfDirective is not found.")
    }

    private fun getElseifDirectives(): List<SqlNode.ElseifDirective> {
        val list = LinkedList<SqlNode.ElseifDirective>()
        while (true) {
            val node = nodeList.peek() as? SqlNode.ElseifDirective
            if (node != null) {
                nodeList.pop()
                list.add(node)
            } else {
                return list
            }
        }
    }

    private fun getElseDirective(): SqlNode.ElseDirective? {
        val node = nodeList.peek() as? SqlNode.ElseDirective
        if (node != null) {
            nodeList.pop()
        }
        return node
    }

    private fun getEndDirective(): SqlNode.EndDirective {
        return when (val node = nodeList.poll()) {
            is SqlNode.EndDirective -> node
            is SqlNode.ElseifDirective -> throw SqlException("The illegal elseif directive is found at ${node.location}")
            is SqlNode.ElseDirective -> throw SqlException("The illegal else directive is found at ${node.location}")
            else -> throw throw SqlException("The corresponding end directive is not found at $location")
        }
    }
}

internal class ForBlockReducer(private val location: SqlLocation) : BlockReducer() {

    override fun addNode(node: SqlNode) = when (node) {
        is SqlNode.ForDirective, is SqlNode.EndDirective -> super.addNode(node)
        else -> error(node)
    }

    override fun reduce(): SqlNode {
        val forDirective = getForDirective()
        val endDirective = getEndDirective()
        return SqlNode.ForBlock(forDirective, endDirective)
    }

    private fun getForDirective(): SqlNode.ForDirective {
        val node = nodeList.poll() as? SqlNode.ForDirective
        return node ?: error("ForDirective is not found.")
    }

    private fun getEndDirective(): SqlNode.EndDirective = when (val node = nodeList.poll()) {
        is SqlNode.EndDirective -> node
        else -> throw throw SqlException("The corresponding end directive is not found at $location")
    }
}

internal class WithBlockReducer(private val location: SqlLocation) : BlockReducer() {

    override fun addNode(node: SqlNode) = when (node) {
        is SqlNode.WithDirective, is SqlNode.EndDirective -> super.addNode(node)
        else -> error(node)
    }

    override fun reduce(): SqlNode {
        val withDirective = getWithDirective()
        val endDirective = getEndDirective()
        return SqlNode.WithBlock(withDirective, endDirective)
    }

    private fun getWithDirective(): SqlNode.WithDirective {
        val node = nodeList.poll() as? SqlNode.WithDirective
        return node ?: error("WithDirective is not found.")
    }

    private fun getEndDirective(): SqlNode.EndDirective = when (val node = nodeList.poll()) {
        is SqlNode.EndDirective -> node
        else -> throw throw SqlException("The corresponding end directive is not found at $location")
    }
}

internal class IfDirectiveReducer(private val location: SqlLocation, private val token: String, private val expression: String) :
    SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.IfDirective(location, token, expression, nodeList)
}

internal class ElseifDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val expression: String,
) :
    SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.ElseifDirective(location, token, expression, nodeList)
}

internal class ElseDirectiveReducer(private val location: SqlLocation, private val token: String) : SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.ElseDirective(location, token, nodeList)
}

internal class ForDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val identifier: String,
    private val expression: String,
) :
    SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.ForDirective(location, token, identifier, expression, nodeList)
}

internal class WithDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val leftExpression: String,
    private val rightExpression: String?,
) :
    SqlReducer() {
    override fun reduce(): SqlNode =
        SqlNode.WithDirective(location, token, leftExpression, rightExpression, nodeList)
}
