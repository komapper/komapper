package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.Value
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ArithmeticExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.StringFunction
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.query.ScalarQuery

class BuilderSupport(
    private val dialect: Dialect,
    private val aliasManager: AliasManager,
    private val buf: StatementBuffer,
    private val escapeSequence: String? = null
) {
    private val operation = CriterionOperation()

    fun visitTableExpression(expression: TableExpression<*>, nameType: TableNameType) {
        val name = expression.getCanonicalTableName(dialect::enquote)
        when (nameType) {
            TableNameType.NAME_ONLY -> {
                buf.append(name)
            }
            TableNameType.ALIAS_ONLY -> {
                val alias = aliasManager.getAlias(expression) ?: error("Alias is not found. table=$name ,sql=$buf")
                buf.append(alias)
            }
            TableNameType.NAME_AND_ALIAS -> {
                val alias = aliasManager.getAlias(expression) ?: error("Alias is not found. table=$name ,sql=$buf")
                if (alias.isBlank()) {
                    buf.append(name)
                } else {
                    buf.append("$name as $alias")
                }
            }
        }
    }

    fun visitColumnExpression(expression: ColumnExpression<*>) {
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
                visitColumnExpression(function.expression)
                buf.append(")")
            }
            is AggregateFunction.CountAsterisk -> {
                buf.append("count(*)")
            }
            is AggregateFunction.Count -> {
                buf.append("count(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }
            is AggregateFunction.Max -> {
                buf.append("max(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }
            is AggregateFunction.Min<*> -> {
                buf.append("min(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }
            is AggregateFunction.Sum<*> -> {
                buf.append("sum(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }
        }
    }

    private fun visitAliasExpression(expression: AliasExpression<*>) {
        visitColumnExpression(expression.expression)
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

    fun buildSubqueryStatement(subqueryContext: SubqueryContext<*>): Statement {
        return when (subqueryContext) {
            is SubqueryContext.EntitySelect<*> -> {
                val context = subqueryContext.context
                val childAliasManager = DefaultAliasManager(context, aliasManager)
                val builder = EntitySelectStatementBuilder(dialect, context, childAliasManager)
                builder.build()
            }
            is SubqueryContext.SqlSelect<*> -> {
                val context = subqueryContext.context
                val childAliasManager = DefaultAliasManager(context, aliasManager)
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

    fun visitOperand(operand: Operand) {
        when (operand) {
            is Operand.Column -> {
                visitColumnExpression(operand.expression)
            }
            is Operand.Argument -> {
                visitArgument(operand)
            }
        }
    }

    private fun visitArgument(argument: Operand.Argument) {
        buf.bind(Value(argument.value, argument.klass))
    }

    fun visitCriterion(index: Int, c: Criterion) {
        when (c) {
            is Criterion.Eq -> operation.binary(c.left, c.right, "=")
            is Criterion.NotEq -> operation.binary(c.left, c.right, "<>")
            is Criterion.Less -> operation.binary(c.left, c.right, "<")
            is Criterion.LessEq -> operation.binary(c.left, c.right, "<=")
            is Criterion.Grater -> operation.binary(c.left, c.right, ">")
            is Criterion.GraterEq -> operation.binary(c.left, c.right, ">=")
            is Criterion.IsNull -> operation.isNull(c.left)
            is Criterion.IsNotNull -> operation.isNull(c.left, true)
            is Criterion.Like -> operation.like(c.left, c.right)
            is Criterion.NotLike -> operation.like(c.left, c.right, true)
            is Criterion.Between -> operation.between(c.left, c.right)
            is Criterion.NotBetween -> operation.between(c.left, c.right, true)
            is Criterion.InList -> operation.inList(c.left, c.right)
            is Criterion.NotInList -> operation.inList(c.left, c.right, true)
            is Criterion.InList2 -> operation.inList2(c.left, c.right)
            is Criterion.NotInList2 -> operation.inList2(c.left, c.right, true)
            is Criterion.InSubQuery -> operation.inSubQuery(c.left, c.right)
            is Criterion.NotInSubQuery -> operation.inSubQuery(c.left, c.right, true)
            is Criterion.InSubQuery2 -> operation.inSubQuery2(c.left, c.right)
            is Criterion.NotInSubQuery2 -> operation.inSubQuery2(c.left, c.right, true)
            is Criterion.Exists -> operation.exists(c.context)
            is Criterion.NotExists -> operation.exists(c.context, true)
            is Criterion.And -> operation.logicalBinary("and", c.criteria, index)
            is Criterion.Or -> operation.logicalBinary("or", c.criteria, index)
            is Criterion.Not -> operation.not(c.criteria)
        }
    }

    private inner class CriterionOperation {

        fun binary(left: Operand, right: Operand, operator: String) {
            visitOperand(left)
            buf.append(" $operator ")
            visitOperand(right)
        }

        fun isNull(left: Operand, not: Boolean = false) {
            visitOperand(left)
            val predicate = if (not) {
                " is not null"
            } else {
                " is null"
            }
            buf.append(predicate)
        }

        fun like(left: Operand, right: Operand, not: Boolean = false) {
            visitOperand(left)
            if (not) {
                buf.append(" not")
            }
            buf.append(" like ")
            when (right) {
                is Operand.Column -> {
                    visitColumnExpression(right.expression)
                }
                is Operand.Argument -> {
                    when (val value = right.value) {
                        is EscapeExpression -> {
                            val finalEscapeSequence = escapeSequence ?: dialect.escapeSequence
                            val newValue = escape(value) { dialect.escape(it, finalEscapeSequence) }
                            visitArgument(Operand.Argument(right.klass, newValue))
                            buf.append(" escape ")
                            buf.bind(Value(finalEscapeSequence, String::class))
                        }
                        else -> visitArgument(right)
                    }
                }
            }
        }

        private fun escape(expression: EscapeExpression, escape: (String) -> String): String {
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

        fun between(left: Operand, right: Pair<Operand, Operand>, not: Boolean = false) {
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

        fun inList(left: Operand, right: List<Operand>, not: Boolean = false) {
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

        fun inList2(
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

        fun inSubQuery(left: Operand, right: SubqueryContext<*>, not: Boolean = false) {
            visitOperand(left)
            if (not) {
                buf.append(" not")
            }
            buf.append(" in (")
            val statement = buildSubqueryStatement(right)
            buf.append(statement)
            buf.append(")")
        }

        fun inSubQuery2(left: Pair<Operand, Operand>, right: SubqueryContext<*>, not: Boolean = false) {
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

        fun exists(subqueryContext: SubqueryContext<*>, not: Boolean = false) {
            if (not) {
                buf.append("not ")
            }
            buf.append("exists (")
            val statement = buildSubqueryStatement(subqueryContext)
            buf.append(statement)
            buf.append(")")
        }

        fun logicalBinary(operator: String, criteria: List<Criterion>, index: Int) {
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

        fun not(criteria: List<Criterion>) {
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
    }
}
