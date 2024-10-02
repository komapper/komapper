package org.komapper.template

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.Value
import org.komapper.core.template.expression.ExprContext
import org.komapper.core.template.expression.ExprException
import org.komapper.core.template.sql.SqlException
import org.komapper.core.template.sql.SqlLocation
import org.komapper.core.template.sql.SqlNode
import org.komapper.core.template.sql.SqlNodeFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

internal class TwoWayTemplateStatementBuilder(
    private val dialect: BuilderDialect,
    private val sqlNodeFactory: SqlNodeFactory,
    private val exprEvaluator: ExprEvaluator,
) : TemplateStatementBuilder {

    private val clauseRegex = Regex(
        """^(select|from|where|group by|having|order by|for update|option)\s""",
        RegexOption.IGNORE_CASE,
    )

    override fun build(
        template: CharSequence,
        valueMap: Map<String, Value<*>>,
        builtinExtensions: TemplateBuiltinExtensions,
    ): Statement {
        val ctx = ExprContext(valueMap, builtinExtensions)
        return build(template.toString(), ctx)
    }

    private fun build(
        sql: CharSequence,
        ctx: ExprContext,
    ): Statement {
        val node = sqlNodeFactory.get(sql)
        val state = visit(State(ctx), node)
        return state.toStatement()
    }

    private fun visit(state: State, node: SqlNode): State = when (node) {
        is SqlNode.Statement -> node.nodeList.fold(state, ::visit)
        is SqlNode.Set -> {
            val left = visit(State(state.asExprContext()), node.left)
            if (left.available) {
                state.available = true
                state.append(left)
            }
            val right = visit(State(state.asExprContext()), node.right)
            if (right.available) {
                if (left.available) {
                    state.append(node.keyword)
                }
                state.available = true
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
            val childState = node.nodeList.fold(State(state.asExprContext()), ::visit)
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
            if (node is SqlNode.Blank) {
                state.append(node)
            } else {
                state.append(node.token)
            }
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

                else -> {
                    val value = rebuildValue(result)
                    state.bind(value)
                }
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

        is SqlNode.PartialDirective -> {
            error("PartialDirective \"${node.token}\" is not supported in this builder. Use @KomapperCommand.")
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
                        val elseDirective = node.elseDirective
                        if (elseDirective != null) {
                            return elseDirective.nodeList
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
            val idComma = id + "_next_comma"
            val idAnd = id + "_next_and"
            val idOr = id + "_next_or"
            while (it.hasNext()) {
                val each = it.next()
                s.valueMap[id] = newValue(each)
                s.valueMap[idIndex] = Value(index++, typeOf<Int>())
                s.valueMap[idHasNext] = Value(it.hasNext(), typeOf<Boolean>())
                s.valueMap[idComma] = Value(if (it.hasNext()) "," else "", typeOf<String>())
                s.valueMap[idAnd] = Value(if (it.hasNext()) "and" else "", typeOf<String>())
                s.valueMap[idOr] = Value(if (it.hasNext()) "or" else "", typeOf<String>())
                s = node.forDirective.nodeList.fold(s, ::visit)
            }
            if (preserved != null) {
                s.valueMap[id] = preserved
            }
            s.valueMap.remove(idIndex)
            s.valueMap.remove(idHasNext)
            s.valueMap.remove(idComma)
            s.valueMap.remove(idAnd)
            s.valueMap.remove(idOr)
            s
        }

        is SqlNode.WithBlock -> {
            val withDirective = node.withDirective
            val (receiver, type) = eval(withDirective.location, withDirective.expression, state.asExprContext())
            if (receiver == null) {
                throw SqlException("The expression \"${withDirective.expression}\" is null at ${withDirective.location}")
            }
            val klass = type.classifier as? KClass<*> ?: throw SqlException("The expression \"${withDirective.expression}\" is not a class at ${withDirective.location}")
            val preserved = HashMap(state.valueMap)
            for (property in klass.memberProperties) {
                val v = property.call(receiver)
                state.valueMap[property.name] = Value(v, property.returnType)
            }
            withDirective.nodeList.fold(state, ::visit)
            state.valueMap.clear()
            state.valueMap.putAll(preserved)
            state
        }

        is SqlNode.IfDirective,
        is SqlNode.ElseifDirective,
        is SqlNode.ElseDirective,
        is SqlNode.EndDirective,
        is SqlNode.ForDirective,
        is SqlNode.WithDirective,
        -> error("unreachable")
    }

    private fun newValue(o: Any?): Value<*> {
        val value = Value(o, o?.let { it::class.createType() } ?: typeOf<Any>())
        return rebuildValue(value)
    }

    private fun rebuildValue(value: Value<*>): Value<*> {
        val klass = value.type.classifier as KClass<*>
        return if (klass.isValue) {
            val parameter = klass.primaryConstructor?.parameters?.firstOrNull()
                ?: error("The parameter is not found for the primary constructor of ${klass.qualifiedName}.")
            val property = klass.declaredMemberProperties.firstOrNull { it.name == parameter.name }
                ?: error("The property is not found. parameter=${parameter.name}, class=${klass.qualifiedName}.")
            val v = when (val any = value.any) {
                null -> null
                else -> {
                    @Suppress("UNCHECKED_CAST")
                    property as KProperty1<Any, *>
                    property.isAccessible = true
                    property.get(any)
                }
            }
            Value(v, property.returnType, value.masking)
        } else {
            value
        }
    }

    private fun eval(location: SqlLocation, expression: String, ctx: ExprContext): Value<*> = try {
        exprEvaluator.eval(expression, ctx)
    } catch (e: ExprException) {
        throw SqlException("The expression evaluation failed. ${e.message} at $location.", e)
    }

    override fun clearCache() {
        sqlNodeFactory.clearCache()
        exprEvaluator.clearCache()
    }

    inner class State(private val ctx: ExprContext) {

        private val blankNodes = mutableListOf<SqlNode.Blank>()
        private var eolNodeCount = 0

        private val buf = StatementBuffer()
        val valueMap: MutableMap<String, Value<*>> = HashMap(ctx.valueMap)
        var available: Boolean = false

        fun asExprContext(): ExprContext {
            return ExprContext(valueMap, ctx.builtinExtensions)
        }

        fun append(state: State): State {
            flushBlankNodes()
            state.flushBlankNodes()

            buf.parts.addAll(state.buf.parts)
            return this
        }

        fun append(s: CharSequence): State {
            flushBlankNodes()
            buf.append(s)
            return this
        }

        fun append(blank: SqlNode.Blank): State {
            blankNodes.add(blank)
            if (blank is SqlNode.Token.Eol) {
                eolNodeCount++
            }
            return this
        }

        fun bind(value: Value<*>): State {
            flushBlankNodes()
            buf.bind(value)
            return this
        }

        fun startsWithClause(): Boolean {
            flushBlankNodes()
            val s = buf.toString().trim()
            return clauseRegex.containsMatchIn(s)
        }

        fun flushBlankNodes() {
            if (blankNodes.isEmpty()) return
            val blank = convertBlankNodesToString()
            buf.append(blank)
            blankNodes.clear()
            eolNodeCount = 0
        }

        private fun convertBlankNodesToString(): String {
            if (eolNodeCount > 0) {
                var seenEolNodeCount = 0
                val iterator: MutableListIterator<SqlNode.Blank> = blankNodes.listIterator()
                while (iterator.hasNext()) {
                    val node = iterator.next()
                    if (node is SqlNode.Token.Eol) {
                        seenEolNodeCount++
                        if (seenEolNodeCount >= eolNodeCount) {
                            break
                        }
                    }
                    iterator.remove()
                }
            }
            return blankNodes.joinToString(separator = "") { it.token }
        }

        fun toStatement(): Statement {
            return buf.toStatement()
        }

        override fun toString() = buf.toString()
    }
}
