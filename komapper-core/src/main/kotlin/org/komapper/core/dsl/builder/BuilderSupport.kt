package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.data.Criterion
import org.komapper.core.dsl.data.Operand
import org.komapper.core.dsl.option.LikeOption
import org.komapper.core.dsl.query.AggregateFunction
import org.komapper.core.dsl.util.getName
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo

internal class BuilderSupport(
    private val dialect: Dialect,
    private val aliasManager: AliasManager,
    private val buf: StatementBuffer
) {

    fun aliasTableName(tableInfo: TableInfo): String {
        val name = tableInfo.getName(dialect::quote)
        val alias = aliasManager.getAlias(tableInfo) ?: error("no alias for '${tableInfo.tableName()}'")
        return "$name $alias"
    }

    fun aliasColumnName(columnInfo: ColumnInfo<*>): String {
        return when (columnInfo) {
            is AggregateFunction.Avg -> {
                val name = aliasColumnName(columnInfo.c)
                "avg($name)"
            }
            is AggregateFunction.CountAsterisk -> {
                "count(*)"
            }
            is AggregateFunction.Count -> {
                val name = aliasColumnName(columnInfo.c)
                "count($name)"
            }
            is AggregateFunction.Max -> {
                val name = aliasColumnName(columnInfo.c)
                "max($name)"
            }
            is AggregateFunction.Min -> {
                val name = aliasColumnName(columnInfo.c)
                "min($name)"
            }
            is AggregateFunction.Sum -> {
                val name = aliasColumnName(columnInfo.c)
                "sum($name)"
            }
            else -> {
                val name = columnInfo.getName(dialect::quote)
                val alias = aliasManager.getAlias(columnInfo) ?: error("no alias for $name")
                return "$alias.$name"
            }
        }
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
            is Criterion.Like -> likeOperation(c.left, c.right, c.option)
            is Criterion.NotLike -> likeOperation(c.left, c.right, c.option, true)
            is Criterion.Between -> betweenOperation(c.left, c.right)
            is Criterion.NotBetween -> betweenOperation(c.left, c.right, true)
            is Criterion.InList -> inListOperation(c.left, c.right)
            is Criterion.NotInList -> inListOperation(c.left, c.right, true)
            is Criterion.InSubQuery -> inSubQueryOperation(c.left, c.right)
            is Criterion.NotInSubQuery -> inSubQueryOperation(c.left, c.right, true)
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

    private fun likeOperation(left: Operand, right: Operand, option: LikeOption, not: Boolean = false) {
        visitOperand(left)
        if (not) {
            buf.append(" not")
        }
        buf.append(" like ")
        visitLikeOperand(right, option)
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

    private fun visitLikeOperand(operand: Operand, option: LikeOption) {
        fun bind(value: Any?, mapper: (String) -> String, escape: (String) -> String) {
            if (value == null) {
                buf.bind(Value(null, String::class))
            } else {
                val text = mapper(escape(value.toString()))
                buf.bind(Value(text, String::class))
            }
        }
        when (operand) {
            is Operand.Column -> {
                buf.append(aliasColumnName(operand.columnInfo))
            }
            is Operand.Parameter -> {
                val value = operand.value
                val escape = dialect::escape
                when (option) {
                    is LikeOption.None -> bind(value, { it }, { it })
                    is LikeOption.Escape -> bind(value, { it }, escape)
                    is LikeOption.Prefix -> bind(value, { "$it%" }, escape)
                    is LikeOption.Infix -> bind(value, { "%$it%" }, escape)
                    is LikeOption.Suffix -> bind(value, { "%$it" }, escape)
                }
            }
        }
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

    private fun inSubQueryOperation(left: Operand, right: SqlSelectContext<*>, not: Boolean = false) {
        visitOperand(left)
        if (not) {
            buf.append(" not")
        }
        buf.append(" in (")
        val childAliasManager = AliasManager(right, aliasManager)
        val builder = SqlSelectStatementBuilder(dialect, right, childAliasManager)
        buf.append(builder.build())
        buf.append(")")
    }

    private fun existsOperation(subContext: SqlSelectContext<*>, not: Boolean = false) {
        if (not) {
            buf.append("not ")
        }
        buf.append("exists (")
        val childAliasManager = AliasManager(subContext, aliasManager)
        val builder = SqlSelectStatementBuilder(dialect, subContext, childAliasManager)
        buf.append(builder.build())
        buf.append(")")
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
            is Operand.Column -> {
                buf.append(aliasColumnName(operand.columnInfo))
            }
            is Operand.Parameter -> {
                buf.bind(Value(operand.value, operand.columnInfo.klass))
            }
        }
    }
}
