package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ArithmeticExpression
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.expression.StringFunction
import org.komapper.core.dsl.query.ScalarQuery

class BuilderSupport(
    private val dialect: Dialect,
    private val aliasManager: AliasManager,
    private val buf: StatementBuffer,
    private val escapeSequence: String? = null
) {

    fun visitEntityExpression(expression: EntityExpression<*>) {
        val name = expression.getCanonicalTableName(dialect::enquote)
        val alias = aliasManager.getAlias(expression) ?: error("Alias is not found. table=$name ,sql=$buf")
        if (alias.isEmpty()) {
            buf.append(name)
        } else {
            buf.append("$name $alias")
        }
    }

    fun visitPropertyExpression(expression: PropertyExpression<*>) {
        when (expression) {
            is AggregateFunction -> {
                visitAggregateFunction(expression)
            }
            is AliasExpression -> {
                visitAliasExpression(expression)
            }
            is ArithmeticExpression -> {
                visitArithmeticExpression(expression)
            }
            is ScalarQuery<*, *> -> {
                visitSingleProjectionQuery(expression)
            }
            is StringFunction -> {
                visitStringFunction(expression)
            }
            else -> {
                val name = expression.getCanonicalColumnName(dialect::enquote)
                val owner = expression.owner
                val alias = aliasManager.getAlias(owner)
                    ?: error("Alias is not found. table=${owner.getCanonicalTableName(dialect::enquote)}, column=$name ,sql=$buf")
                if (alias.isBlank()) {
                    buf.append(name)
                } else {
                    buf.append("$alias.$name")
                }
            }
        }
    }

    private fun visitAggregateFunction(function: AggregateFunction<*>) {
        when (function) {
            is AggregateFunction.Avg -> {
                buf.append("avg(")
                visitPropertyExpression(function.expression)
                buf.append(")")
            }
            is AggregateFunction.CountAsterisk -> {
                buf.append("count(*)")
            }
            is AggregateFunction.Count -> {
                buf.append("count(")
                visitPropertyExpression(function.expression)
                buf.append(")")
            }
            is AggregateFunction.Max -> {
                buf.append("max(")
                visitPropertyExpression(function.expression)
                buf.append(")")
            }
            is AggregateFunction.Min<*> -> {
                buf.append("min(")
                visitPropertyExpression(function.expression)
                buf.append(")")
            }
            is AggregateFunction.Sum<*> -> {
                buf.append("sum(")
                visitPropertyExpression(function.expression)
                buf.append(")")
            }
        }
    }

    private fun visitAliasExpression(expression: AliasExpression<*>) {
        visitPropertyExpression(expression.expression)
        buf.append(" as ${dialect.enquote(expression.alias)}")
    }

    private fun visitArithmeticExpression(expression: ArithmeticExpression<*>) {
        buf.append("(")
        when (expression) {
            is ArithmeticExpression.Plus<*> -> {
                visitOperand(expression.left)
                buf.append(" + ")
                visitOperand(expression.right)
            }
            is ArithmeticExpression.Minus<*> -> {
                visitOperand(expression.left)
                buf.append(" - ")
                visitOperand(expression.right)
            }
            is ArithmeticExpression.Times<*> -> {
                visitOperand(expression.left)
                buf.append(" * ")
                visitOperand(expression.right)
            }
            is ArithmeticExpression.Div<*> -> {
                visitOperand(expression.left)
                buf.append(" / ")
                visitOperand(expression.right)
            }
            is ArithmeticExpression.Rem<*> -> {
                visitOperand(expression.left)
                buf.append(" % ")
                visitOperand(expression.right)
            }
        }
        buf.append(")")
    }

    private fun visitSingleProjectionQuery(expression: ScalarQuery<*, *>) {
        buf.append("(")
        val statement = buildSubqueryStatement(expression.subqueryContext)
        buf.append(statement)
        buf.append(")")
    }

    private fun visitStringFunction(function: StringFunction) {
        buf.append("(")
        when (function) {
            is StringFunction.Concat -> {
                buf.append("concat(")
                visitOperand(function.left)
                buf.append(", ")
                visitOperand(function.right)
                buf.append(")")
            }
        }
        buf.append(")")
    }

    fun visitCriterion(index: Int, c: Criterion) {
        when (c) {
            is Criterion.Eq -> binaryOperation(c.left, c.right, "=")
            is Criterion.NotEq -> binaryOperation(c.left, c.right, "<>")
            is Criterion.Less -> binaryOperation(c.left, c.right, "<")
            is Criterion.LessEq -> binaryOperation(c.left, c.right, "<=")
            is Criterion.Grater -> binaryOperation(c.left, c.right, ">")
            is Criterion.GraterEq -> binaryOperation(c.left, c.right, ">=")
            is Criterion.IsNull -> isNullOperation(c.left)
            is Criterion.IsNotNull -> isNullOperation(c.left, true)
            is Criterion.Like -> likeOperation(c.left, c.right)
            is Criterion.NotLike -> likeOperation(c.left, c.right, true)
            is Criterion.Between -> betweenOperation(c.left, c.right)
            is Criterion.NotBetween -> betweenOperation(c.left, c.right, true)
            is Criterion.InList -> inListOperation(c.left, c.right)
            is Criterion.NotInList -> inListOperation(c.left, c.right, true)
            is Criterion.InList2 -> inList2Operation(c.left, c.right)
            is Criterion.NotInList2 -> inList2Operation(c.left, c.right, true)
            is Criterion.InSubQuery -> inSubQueryOperation(c.left, c.right)
            is Criterion.NotInSubQuery -> inSubQueryOperation(c.left, c.right, true)
            is Criterion.InSubQuery2 -> inSubQuery2Operation(c.left, c.right)
            is Criterion.NotInSubQuery2 -> inSubQuery2Operation(c.left, c.right, true)
            is Criterion.Exists -> existsOperation(c.context)
            is Criterion.NotExists -> existsOperation(c.context, true)
            is Criterion.And -> logicalBinaryOperation("and", c.criteria, index)
            is Criterion.Or -> logicalBinaryOperation("or", c.criteria, index)
            is Criterion.Not -> notOperation(c.criteria)
        }
    }

    private fun binaryOperation(left: Operand, right: Operand, operator: String) {
        visitOperand(left)
        buf.append(" $operator ")
        visitOperand(right)
    }

    private fun isNullOperation(left: Operand, not: Boolean = false) {
        visitOperand(left)
        val predicate = if (not) {
            " is not null"
        } else {
            " is null"
        }
        buf.append(predicate)
    }

    private fun likeOperation(left: Operand, right: Operand, not: Boolean = false) {
        visitOperand(left)
        if (not) {
            buf.append(" not")
        }
        buf.append(" like ")
        visitLikeRightOperand(right)
    }

    private fun visitLikeRightOperand(operand: Operand) {
        when (operand) {
            is Operand.Property -> {
                visitPropertyExpression(operand.expression)
            }
            is Operand.Parameter -> {
                when (val value = operand.value) {
                    is EscapeExpression -> {
                        val finalEscapeSequence = escapeSequence ?: dialect.escapeSequence
                        val newValue = visitEscapeExpression(value) { dialect.escape(it, finalEscapeSequence) }
                        visitParameter(Operand.Parameter(operand.expression, newValue))
                        buf.append(" escape ")
                        buf.bind(Value(finalEscapeSequence, String::class))
                    }
                    else -> visitParameter(operand)
                }
            }
        }
    }

    private fun visitEscapeExpression(expression: EscapeExpression, escape: (String) -> String): String {
        val buf = StringBuilder(expression.length + 10)
        fun visit(e: EscapeExpression) {
            when (e) {
                is EscapeExpression.Text -> buf.append(e.value)
                is EscapeExpression.Escape -> buf.append(escape(e.value.toString()))
                is EscapeExpression.Composite -> {
                    visit(e.left)
                    visit(e.right)
                }
            }
        }
        visit(expression)
        return buf.toString()
    }

    private fun betweenOperation(left: Operand, right: Pair<Operand, Operand>, not: Boolean = false) {
        visitOperand(left)
        if (not) {
            buf.append(" not")
        }
        buf.append(" between ")
        val (start, end) = right
        visitOperand(start)
        buf.append(" and ")
        visitOperand(end)
    }

    private fun inListOperation(left: Operand, right: List<Operand>, not: Boolean = false) {
        visitOperand(left)
        if (not) {
            buf.append(" not")
        }
        buf.append(" in (")
        if (right.isEmpty()) {
            buf.append("null")
        } else {
            for (parameter in right) {
                visitOperand(parameter)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        buf.append(")")
    }

    private fun inList2Operation(
        left: Pair<Operand, Operand>,
        right: List<Pair<Operand, Operand>>,
        not: Boolean = false
    ) {
        buf.append("(")
        visitOperand(left.first)
        buf.append(", ")
        visitOperand(left.second)
        buf.append(")")
        if (not) {
            buf.append(" not")
        }
        buf.append(" in (")
        if (right.isEmpty()) {
            buf.append("null")
        } else {
            for ((first, second) in right) {
                buf.append("(")
                visitOperand(first)
                buf.append(", ")
                visitOperand(second)
                buf.append(")")
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        buf.append(")")
    }

    private fun inSubQueryOperation(left: Operand, right: SubqueryContext<*>, not: Boolean = false) {
        visitOperand(left)
        if (not) {
            buf.append(" not")
        }
        buf.append(" in (")
        val statement = buildSubqueryStatement(right)
        buf.append(statement)
        buf.append(")")
    }

    private fun inSubQuery2Operation(left: Pair<Operand, Operand>, right: SubqueryContext<*>, not: Boolean = false) {
        buf.append("(")
        visitOperand(left.first)
        buf.append(", ")
        visitOperand(left.second)
        buf.append(")")
        if (not) {
            buf.append(" not")
        }
        buf.append(" in (")
        val statement = buildSubqueryStatement(right)
        buf.append(statement)
        buf.append(")")
    }

    private fun existsOperation(subqueryContext: SubqueryContext<*>, not: Boolean = false) {
        if (not) {
            buf.append("not ")
        }
        buf.append("exists (")
        val statement = buildSubqueryStatement(subqueryContext)
        buf.append(statement)
        buf.append(")")
    }

    fun buildSubqueryStatement(subqueryContext: SubqueryContext<*>): Statement {
        return when (subqueryContext) {
            is SubqueryContext.EntitySelect<*> -> {
                val context = subqueryContext.context
                val childAliasManager = AliasManagerImpl(context, aliasManager)
                val builder = EntitySelectStatementBuilder(dialect, context, childAliasManager)
                builder.build()
            }
            is SubqueryContext.SqlSelect<*> -> {
                val context = subqueryContext.context
                val childAliasManager = AliasManagerImpl(context, aliasManager)
                val builder = SqlSelectStatementBuilder(dialect, context, childAliasManager)
                builder.build()
            }
            is SubqueryContext.SqlSetOperation<*> -> {
                val context = subqueryContext.context
                val builder = SqlSetOperationStatementBuilder(dialect, context, aliasManager)
                builder.build()
            }
        }
    }

    private fun logicalBinaryOperation(operator: String, criteria: List<Criterion>, index: Int) {
        if (criteria.isNotEmpty()) {
            if (index > 0) {
                buf.cutBack(5)
            }
            if (index != 0) {
                buf.append(" $operator ")
            }
            buf.append("(")
            for ((i, c) in criteria.withIndex()) {
                visitCriterion(i, c)
                buf.append(" and ")
            }
            buf.cutBack(5)
            buf.append(")")
        }
    }

    private fun notOperation(criteria: List<Criterion>) {
        if (criteria.isNotEmpty()) {
            buf.append("not ")
            buf.append("(")
            for ((index, c) in criteria.withIndex()) {
                visitCriterion(index, c)
                buf.append(" and ")
            }
            buf.cutBack(5)
            buf.append(")")
        }
    }

    fun visitOperand(operand: Operand) {
        when (operand) {
            is Operand.Property -> {
                visitPropertyExpression(operand.expression)
            }
            is Operand.Parameter -> {
                visitParameter(operand)
            }
        }
    }

    private fun visitParameter(parameter: Operand.Parameter) {
        buf.bind(Value(parameter.value, parameter.expression.klass))
    }
}
