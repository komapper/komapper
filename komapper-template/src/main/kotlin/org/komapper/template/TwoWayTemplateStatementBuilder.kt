package org.komapper.template

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.Value
import org.komapper.template.expression.ExprContext
import org.komapper.template.expression.ExprEvaluator
import org.komapper.template.expression.ExprException
import org.komapper.template.sql.SqlException
import org.komapper.template.sql.SqlLocation
import org.komapper.template.sql.SqlNode
import org.komapper.template.sql.SqlNodeFactory
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

internal class TwoWayTemplateStatementBuilder(
    private val dialect: Dialect,
    private val sqlNodeFactory: SqlNodeFactory,
    private val exprEvaluator: ExprEvaluator
) : TemplateStatementBuilder {

    private val clauseRegex = Regex(
        """^(select|from|where|group by|having|order by|for update|option)\s""", RegexOption.IGNORE_CASE
    )

    override fun build(template: CharSequence, data: Any, escape: (String) -> String): Statement {
        val ctx = createContext(data, escape)
        return build(template.toString(), ctx)
    }

    private fun createContext(data: Any, escape: (String) -> String): ExprContext {
        val valueMap = data::class.memberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .associate { it.name to Value(it.call(data), it.returnType.jvmErasure) }
        return ExprContext(valueMap, escape)
    }

    fun build(
        sql: CharSequence,
        ctx: ExprContext = ExprContext()
    ): Statement {
        val node = sqlNodeFactory.get(sql)
        val state = visit(State(ctx), node)
        return state.toStatement()
    }

    private fun visit(state: State, node: SqlNode): State = when (node) {
        is SqlNode.Statement -> node.nodeList.fold(state, ::visit)
        is SqlNode.Set -> {
            val left = visit(State(state), node.left)
            if (left.available) {
                state.append(left)
            }
            val right = visit(State(state), node.right)
            if (right.available) {
                if (left.available) {
                    state.append(node.keyword)
                }
                state.append(right)
            }
            state
        }
        is SqlNode.Clause.Select -> {
            state.append(node.keyword)
            node.nodeList.fold(state, ::visit)
        }
        is SqlNode.Clause.From -> {
            state.append(node.keyword)
            node.nodeList.fold(state, ::visit)
        }
        is SqlNode.Clause.ForUpdate -> {
            state.append(node.keyword)
            node.nodeList.fold(state, ::visit)
        }
        is SqlNode.Clause -> {
            val childState = node.nodeList.fold(State(state), ::visit)
            if (childState.available) {
                state.append(node.keyword).append(childState)
            } else if (childState.startsWithClause()) {
                state.available = true
                state.append(childState)
            }
            state
        }
        is SqlNode.BiLogicalOp -> {
            if (state.available) {
                state.append(node.keyword)
            }
            node.nodeList.fold(state, ::visit)
        }
        is SqlNode.Token -> {
            if (node is SqlNode.Token.Word || node is SqlNode.Token.Other) {
                state.available = true
            }
            state.append(node.token)
        }
        is SqlNode.Paren -> {
            state.available = true
            state.append("(")
            visit(state, node.node).append(")")
        }
        is SqlNode.BindValueDirective -> {
            val result = eval(node.location, node.expression, state.asExprContext())
            when (val obj = result.any) {
                is Iterable<*> -> {
                    var counter = 0
                    state.append("(")
                    for (o in obj) {
                        if (++counter > 1) state.append(", ")
                        when (o) {
                            is Pair<*, *> -> {
                                if (!dialect.supportsMultipleColumnsInInPredicate()) {
                                    throw UnsupportedOperationException("Dialect(name=${dialect.driver}) does not support multiple columns in IN predicate.")
                                }
                                val (f, s) = o
                                state.append("(")
                                    .bind(newValue(f)).append(", ")
                                    .bind(newValue(s)).append(")")
                            }
                            is Triple<*, *, *> -> {
                                if (!dialect.supportsMultipleColumnsInInPredicate()) {
                                    throw UnsupportedOperationException("Dialect(name=${dialect.driver}) does not support multiple columns in IN predicate.")
                                }
                                val (f, s, t) = o
                                state.append("(")
                                    .bind(newValue(f)).append(", ")
                                    .bind(newValue(s)).append(", ")
                                    .bind(newValue(t)).append(")")
                            }
                            else -> state.bind(newValue(o))
                        }
                    }
                    if (counter == 0) {
                        state.append("null")
                    }
                    state.append(")")
                }
                else -> state.bind(result)
            }
            node.nodeList.fold(state, ::visit)
        }
        is SqlNode.EmbeddedValueDirective -> {
            val (obj) = eval(node.location, node.expression, state.asExprContext())
            val s = obj?.toString()
            if (!s.isNullOrEmpty()) {
                state.available = true
                state.append(s)
            }
            state
        }
        is SqlNode.LiteralValueDirective -> {
            val (obj, type) = eval(node.location, node.expression, state.asExprContext())
            val literal = dialect.formatValue(obj, type, false)
            state.append(literal)
            node.nodeList.fold(state, ::visit)
        }
        is SqlNode.IfBlock -> {
            fun chooseNodeList(): List<SqlNode> {
                val (result) = eval(node.ifDirective.location, node.ifDirective.expression, state.asExprContext())
                if (result == true) {
                    return node.ifDirective.nodeList
                } else {
                    val elseIfDirective = node.elseifDirectives.find {
                        val (r) = eval(it.location, it.expression, state.asExprContext())
                        r == true
                    }
                    @Suppress("LiftReturnOrAssignment")
                    if (elseIfDirective != null) {
                        return elseIfDirective.nodeList
                    } else {
                        if (node.elseDirective != null) {
                            return node.elseDirective.nodeList
                        } else {
                            return emptyList()
                        }
                    }
                }
            }

            val nodeList = chooseNodeList()
            nodeList.fold(state, ::visit)
        }
        is SqlNode.ForBlock -> {
            val forDirective = node.forDirective
            val id = forDirective.identifier
            val (expression) = eval(node.forDirective.location, node.forDirective.expression, state.asExprContext())
            expression as? Iterable<*>
                ?: throw SqlException("The expression ${forDirective.expression} is not Iterable at ${forDirective.location}")
            val it = expression.iterator()
            var s = state
            val preserved = s.valueMap[id]
            var index = 0
            val idIndex = id + "_index"
            val idHasNext = id + "_has_next"
            while (it.hasNext()) {
                val each = it.next()
                s.valueMap[id] = newValue(each)
                s.valueMap[idIndex] = Value(index++, Int::class)
                s.valueMap[idHasNext] = Value(it.hasNext(), Boolean::class)
                s = node.forDirective.nodeList.fold(s, ::visit)
            }
            if (preserved != null) {
                s.valueMap[id] = preserved
            }
            s.valueMap.remove(idIndex)
            s.valueMap.remove(idHasNext)
            s
        }
        is SqlNode.IfDirective,
        is SqlNode.ElseifDirective,
        is SqlNode.ElseDirective,
        is SqlNode.EndDirective,
        is SqlNode.ForDirective -> error("unreachable")
    }

    private fun newValue(o: Any?) = Value(o, o?.let { it::class } ?: Any::class)

    private fun eval(location: SqlLocation, expression: String, ctx: ExprContext): Value = try {
        exprEvaluator.eval(expression, ctx)
    } catch (e: ExprException) {
        throw SqlException("The expression evaluation was failed at $location.", e)
    }

    override fun clearCache() {
        sqlNodeFactory.clearCache()
        exprEvaluator.clearCache()
    }

    inner class State(private val ctx: ExprContext) {
        constructor(state: State) : this(ExprContext(state.valueMap))

        private val buf = StatementBuffer()
        val valueMap: MutableMap<String, Value> = HashMap(ctx.valueMap)
        var available: Boolean = false

        fun asExprContext(): ExprContext {
            return ExprContext(valueMap, ctx.functionExtensions)
        }

        fun append(state: State): State {
            buf.parts.addAll(state.buf.parts)
            return this
        }

        fun append(s: CharSequence): State {
            buf.append(s)
            return this
        }

        fun bind(value: Value): State {
            buf.bind(value)
            return this
        }

        fun startsWithClause(): Boolean {
            val s = buf.toString().trim()
            return clauseRegex.containsMatchIn(s)
        }

        fun toStatement() = buf.toStatement()

        override fun toString() = buf.toString()
    }
}
