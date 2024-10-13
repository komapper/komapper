package org.komapper.core.template.sql

import org.komapper.core.template.sql.SqlTokenType.AND
import org.komapper.core.template.sql.SqlTokenType.BIND_VALUE_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.CLOSE_PAREN
import org.komapper.core.template.sql.SqlTokenType.DELIMITER
import org.komapper.core.template.sql.SqlTokenType.ELSEIF_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.ELSE_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.EMBEDDED_VALUE_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.END_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.EOF
import org.komapper.core.template.sql.SqlTokenType.EOL
import org.komapper.core.template.sql.SqlTokenType.EXCEPT
import org.komapper.core.template.sql.SqlTokenType.FOR_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.FOR_UPDATE
import org.komapper.core.template.sql.SqlTokenType.FROM
import org.komapper.core.template.sql.SqlTokenType.GROUP_BY
import org.komapper.core.template.sql.SqlTokenType.HAVING
import org.komapper.core.template.sql.SqlTokenType.IF_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.INTERSECT
import org.komapper.core.template.sql.SqlTokenType.LITERAL_VALUE_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.MINUS
import org.komapper.core.template.sql.SqlTokenType.MULTI_LINE_COMMENT
import org.komapper.core.template.sql.SqlTokenType.OPEN_PAREN
import org.komapper.core.template.sql.SqlTokenType.OPTION
import org.komapper.core.template.sql.SqlTokenType.OR
import org.komapper.core.template.sql.SqlTokenType.ORDER_BY
import org.komapper.core.template.sql.SqlTokenType.OTHER
import org.komapper.core.template.sql.SqlTokenType.PARSER_LEVEL_COMMENT_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.PARTIAL_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.QUOTE
import org.komapper.core.template.sql.SqlTokenType.SELECT
import org.komapper.core.template.sql.SqlTokenType.SINGLE_LINE_COMMENT
import org.komapper.core.template.sql.SqlTokenType.SPACE
import org.komapper.core.template.sql.SqlTokenType.UNION
import org.komapper.core.template.sql.SqlTokenType.WHERE
import org.komapper.core.template.sql.SqlTokenType.WITH_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.WORD
import java.util.LinkedList

internal class SqlParser constructor(
    val sql: String,
    private val tokenizer: SqlTokenizer = SqlTokenizer(sql),
) {
    private val reducers: LinkedList<SqlReducer> = LinkedList()
    private var tokenType: SqlTokenType = EOF
    private var token: String = ""
    private val location: SqlLocation
        get() = tokenizer.location

    init {
        reducers.push(StatementReducer())
    }

    fun parse(): SqlNode {
        outer@ while (true) {
            tokenType = tokenizer.next()
            token = tokenizer.token
            when (tokenType) {
                DELIMITER, EOF -> break@outer
                OPEN_PAREN -> {
                    val parser = SqlParser(sql, tokenizer)
                    val node = parser.parse()
                    if (parser.tokenType != CLOSE_PAREN) {
                        throw SqlException("The close paren is not found at $location")
                    }
                    pushNode(SqlNode.Paren(node))
                }
                CLOSE_PAREN -> break@outer
                WORD, QUOTE -> pushNode(SqlNode.Token.Word(token))
                SPACE -> pushNode(SqlNode.Token.Space.of(token))
                OTHER -> pushNode(SqlNode.Token.Other.of(token))
                EOL -> pushNode(SqlNode.Token.Eol(token))
                MULTI_LINE_COMMENT, SINGLE_LINE_COMMENT -> pushNode(SqlNode.Token.Comment(token))
                SELECT -> reducers.push(SelectReducer(location, token))
                FROM -> reducers.push(FromReducer(location, token))
                WHERE -> reducers.push(WhereReducer(location, token))
                GROUP_BY -> reducers.push(GroupByReducer(location, token))
                HAVING -> reducers.push(HavingReducer(location, token))
                ORDER_BY -> reducers.push(OrderByReducer(location, token))
                FOR_UPDATE -> reducers.push(ForUpdateReducer(location, token))
                OPTION -> reducers.push(OptionReducer(location, token))
                AND -> reducers.push(AndReducer(location, token))
                OR -> reducers.push(OrReducer(location, token))
                UNION, EXCEPT, MINUS, INTERSECT -> {
                    val node = reduceAll()
                    reducers.push(SetReducer(location, token, node))
                    reducers.push(StatementReducer())
                }
                BIND_VALUE_DIRECTIVE -> parseBindValueDirective()
                LITERAL_VALUE_DIRECTIVE -> parseLiteralValueDirective()
                EMBEDDED_VALUE_DIRECTIVE -> parseEmbeddedValueDirective()
                IF_DIRECTIVE -> parseIfDirective()
                ELSEIF_DIRECTIVE -> parseElseifDirective()
                ELSE_DIRECTIVE -> parseElseDirective()
                END_DIRECTIVE -> parseEndDirective()
                FOR_DIRECTIVE -> parseForDirective()
                WITH_DIRECTIVE -> parseWithDirective()
                PARTIAL_DIRECTIVE -> parsePartialDirective()
                PARSER_LEVEL_COMMENT_DIRECTIVE -> Unit // do nothing
            }
        }
        return reduceAll()
    }

    private fun parseBindValueDirective() {
        val expression = token.strip("/*", "*/")
        if (expression.isEmpty()) {
            throw SqlException("The expression is not found in the bind value directive at $location")
        }
        reducers.push(BindValueDirectiveReducer(location, token, expression))
    }

    private fun parseLiteralValueDirective() {
        val expression = token.strip("/*^", "*/")
        if (expression.isEmpty()) {
            throw SqlException("The expression is not found in the literal value directive at $location")
        }
        reducers.push(LiteralValueDirectiveReducer(location, token, expression))
    }

    private fun parseEmbeddedValueDirective() {
        val expression = token.strip("/*#", "*/")
        if (expression.isEmpty()) {
            throw SqlException("The expression is not found in the embedded value directive at $location")
        }
        pushNode(SqlNode.EmbeddedValueDirective(location, token, expression))
    }

    private fun parseIfDirective() {
        val statement = token.strip("/*%", "*/")
        val expression = statement.strip("if", "")
        if (expression.isEmpty()) {
            throw SqlException("The expression is not found in the if directive at $location")
        }
        reducers.push(IfBlockReducer(location))
        reducers.push(IfDirectiveReducer(location, token, expression))
    }

    private fun parseElseifDirective() {
        val statement = token.strip("/*%", "*/")
        val expression = statement.strip("elseif", "")
        if (expression.isEmpty()) {
            throw SqlException("The expression is not found in the elseif directive at $location")
        }
        reduceUntil { it is IfBlockReducer }
        if (reducers.isEmpty()) {
            throw SqlException("The corresponding if directive is not found at $location")
        }
        reducers.push(ElseifDirectiveReducer(location, token, expression))
    }

    private fun parseElseDirective() {
        reduceUntil { it is IfBlockReducer }
        if (reducers.isEmpty()) {
            throw SqlException("The corresponding if directive is not found at $location")
        }
        reducers.push(ElseDirectiveReducer(location, token))
    }

    private fun parseEndDirective() {
        reduceUntil { it is BlockReducer }
        if (reducers.isEmpty()) {
            throw SqlException("The corresponding if, for, or with directive is not found at $location")
        }
        pushNode(SqlNode.EndDirective(location, token))
        val block = reducers.pop()
        pushNode(block.reduce())
    }

    private fun parseForDirective() {
        val statement = token.strip("/*%", "*/")
        val iterationExpression = statement.strip("for", "")
        if (iterationExpression.isEmpty()) {
            throw SqlException("The iteration expression is not found in the for directive at $location")
        }
        val pos = iterationExpression.indexOf("in")
        if (pos == -1) {
            throw SqlException("The keyword \"in\" is not found in the iteration expression in the for directive at $location")
        }
        val identifier = iterationExpression.substring(0, pos).trim()
        if (identifier.isEmpty()) {
            throw SqlException("The identifier is not found in the iteration expression in the for directive at $location")
        }
        val iterableExpression = iterationExpression.substring(pos + 2).trim()
        if (iterableExpression.isEmpty()) {
            throw SqlException("The iterable expression is not found in the iteration expression in the for directive at $location")
        }
        reducers.push(ForBlockReducer(location))
        reducers.push(ForDirectiveReducer(location, token, identifier, iterableExpression))
    }

    private fun parseWithDirective() {
        val statement = token.strip("/*%", "*/")
        val expression = statement.strip("with", "").trim()
        if (expression.isEmpty()) {
            throw SqlException("The expression is not found in the with directive at $location")
        }
        reducers.push(WithBlockReducer(location))
        reducers.push(WithDirectiveReducer(location, token, expression))
    }

    private fun parsePartialDirective() {
        val expression = token.strip("/*>", "*/")
        if (expression.isEmpty()) {
            throw SqlException("The expression is not found in the partial directive at $location")
        }
        pushNode(SqlNode.PartialDirective(location, token, expression))
    }

    private fun reduceUntil(predicate: (SqlReducer) -> Boolean) {
        val it = reducers.iterator()
        while (it.hasNext()) {
            val reducer = it.next()
            if (predicate(reducer)) {
                break
            }
            it.remove()
            val node = reducer.reduce()
            pushNode(node)
        }
    }

    private fun reduceAll(): SqlNode {
        val it = reducers.iterator()
        var node: SqlNode? = null
        while (it.hasNext()) {
            val reducer = it.next()
            it.remove()
            node = reducer.reduce()
            pushNode(node)
        }
        return node ?: error("no reducers")
    }

    private fun pushNode(node: SqlNode) {
        reducers.peek()?.addNode(node)
    }
}

private fun String.strip(prefix: String, suffix: String): String {
    return this.substring(prefix.length, this.length - suffix.length).trim()
}
