package org.komapper.jdbc.postgresql

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EmptyAliasManager
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.element.UpdateSet
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.Assignment

class PostgreSqlEntityUpsertStatementBuilder<ENTITY : Any>(
    private val dialect: Dialect,
    private val context: EntityUpsertContext<ENTITY>,
    private val entity: ENTITY
) : EntityUpsertStatementBuilder<ENTITY> {
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, EmptyAliasManager, buf)

    override fun build(): Statement {
        val idProperties = context.entityMetamodel.idProperties()
        val properties = context.entityMetamodel.properties()
        buf.append("insert into ")
        table(context.entityMetamodel)
        buf.append(" ( ")
        for (p in properties.filter { it.idAssignment !is Assignment.Identity<ENTITY, *> }) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values (")
        for (p in properties.filter { it.idAssignment !is Assignment.Identity<ENTITY, *> }) {
            val value = Value(p.getter(entity), p.klass)
            buf.bind(value)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") on conflict (")
        for (p in idProperties) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        when (context.duplicateKeyType) {
            DuplicateKeyType.IGNORE -> {
                buf.append(" do nothing")
            }
            DuplicateKeyType.UPDATE -> {
                buf.append(" do update set ")
                when (val set = context.updateSet) {
                    is UpdateSet.Pairs<ENTITY> -> {
                        for ((left, right) in set.pairs) {
                            visitOperand(left)
                            buf.append(" = ")
                            visitOperand(right)
                            buf.append(", ")
                        }
                        buf.cutBack(2)
                    }
                    is UpdateSet.Properties<ENTITY> -> {
                        for (p in set.properties) {
                            column(p)
                            buf.append(" = ")
                            val value = Value(p.getter(entity), p.klass)
                            buf.bind(value)
                            buf.append(", ")
                        }
                        buf.cutBack(2)
                    }
                }
            }
        }
        return buf.toStatement()
    }

    private fun table(expression: EntityExpression<*>) {
        support.visitEntityExpression(expression)
    }

    private fun column(expression: PropertyExpression<*>) {
        support.visitPropertyExpression(expression)
    }

    private fun visitOperand(operand: Operand) {
        support.visitOperand(operand)
    }
}
