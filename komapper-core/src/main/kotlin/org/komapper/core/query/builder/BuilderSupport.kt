package org.komapper.core.query.builder

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.EntitySelectContext
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.Operand
import org.komapper.core.query.option.LikeOption

internal class BuilderSupport(
    private val config: DatabaseConfig,
    private val aliasManager: AliasManager,
    private val buf: StatementBuffer
) {

    fun tableName(entityMetamodel: EntityMetamodel<*>): String {
        val alias = aliasManager.getAlias(entityMetamodel) ?: error("no alias")
        return entityMetamodel.tableName() + " " + alias
    }

    fun columnName(propertyMetamodel: PropertyMetamodel<*, *>): String {
        val alias = aliasManager.getAlias(propertyMetamodel) ?: error("no alias")
        return alias + "." + propertyMetamodel.columnName
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
        fun bind(value: Any?, mapper: (CharSequence) -> String, escape: (CharSequence) -> (CharSequence)) {
            if (value == null) {
                buf.bind(Value(null, String::class))
            } else {
                val text = mapper(escape(value.toString()))
                buf.bind(Value(text.toString(), String::class))
            }
        }
        when (operand) {
            is Operand.Property -> {
                buf.append(columnName(operand.metamodel))
            }
            is Operand.Parameter -> {
                val value = operand.value
                val escape = config.dialect::escape
                when (option) {
                    is LikeOption.None -> bind(value, { it.toString() }, { it })
                    is LikeOption.Escape -> bind(value, { it.toString() }, escape)
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

    private fun inSubQueryOperation(left: Operand, right: EntitySelectContext<*>, not: Boolean = false) {
        visitOperand(left)
        if (not) {
            buf.append(" not")
        }
        buf.append(" in (")
        val childAliasManager = AliasManager(right, aliasManager)
        val builder = EntitySelectStatementBuilder(config, right, childAliasManager)
        buf.append(builder.build())
        buf.append(")")
    }

    private fun existsOperation(subContext: EntitySelectContext<*>, not: Boolean = false) {
        if (not) {
            buf.append("not ")
        }
        buf.append("exists (")
        val childAliasManager = AliasManager(subContext, aliasManager)
        val builder = EntitySelectStatementBuilder(config, subContext, childAliasManager)
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

    private fun visitOperand(operand: Operand) {
        when (operand) {
            is Operand.Property -> {
                buf.append(columnName(operand.metamodel))
            }
            is Operand.Parameter -> {
                buf.bind(Value(operand.value, operand.metamodel.klass))
            }
        }
    }
}
