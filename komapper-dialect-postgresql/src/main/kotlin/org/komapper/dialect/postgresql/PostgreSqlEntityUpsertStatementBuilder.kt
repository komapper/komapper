package org.komapper.dialect.postgresql

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.AliasManager
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EmptyAliasManager
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.TableNameType
import org.komapper.core.dsl.builder.getIndexCriteria
import org.komapper.core.dsl.builder.getWhereCriteria
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getNonAutoIncrementProperties

class PostgreSqlEntityUpsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertStatementBuilder<ENTITY> {
    private val target = context.target
    private val excluded = context.excluded
    private val aliasManager = UpsertAliasManager(target, excluded)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf, context.insertContext.options.escapeSequence)
    private val postgreSqlSupport = PostgreSqlStatementBuilderSupport(dialect, context)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val properties = target.getNonAutoIncrementProperties()
        buf.append("insert into ")
        table(target)
        buf.append(" (")
        for (p in properties) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values ")
        for (entity in entities) {
            buf.append("(")
            for (p in properties) {
                buf.bind(p.toValue(entity))
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append("), ")
        }
        buf.cutBack(2)
        when (context.duplicateKeyType) {
            DuplicateKeyType.IGNORE -> {
                onConflict(context.keys)
                buf.append(" do nothing")
            }
            DuplicateKeyType.UPDATE -> {
                onConflict(context.keys.ifEmpty { context.target.idProperties() })
                buf.append(" do update set ")
                for ((left, right) in assignments) {
                    column(left)
                    buf.append(" = ")
                    operand(right)
                    buf.append(", ")
                }
                buf.cutBack(2)
                val criteria = context.getWhereCriteria()
                if (criteria.isNotEmpty()) {
                    buf.append(" where ")
                    for ((index, criterion) in criteria.withIndex()) {
                        criterion(index, criterion)
                        buf.append(" and ")
                    }
                    buf.cutBack(5)
                }
            }
        }
        buf.appendIfNotEmpty(postgreSqlSupport.buildReturning())
        return buf.toStatement()
    }

    private fun onConflict(keys: List<PropertyMetamodel<ENTITY, *, *>>) {
        buf.append(" on conflict")
        val conflictTarget = context.conflictTarget
        if (conflictTarget != null) {
            buf.append(" ")
            buf.append(conflictTarget)
        } else {
            if (keys.isNotEmpty()) {
                buf.append(" (")
                for (p in keys) {
                    column(p)
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append(")")
            }
            val criteria = context.getIndexCriteria()
            if (criteria.isNotEmpty()) {
                val support = BuilderSupport(dialect, EmptyAliasManager, buf, context.insertContext.options.escapeSequence)
                buf.append(" where ")
                for ((index, criterion) in criteria.withIndex()) {
                    support.visitCriterion(index, criterion)
                    buf.append(" and ")
                }
                buf.cutBack(5)
            }
        }
    }

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_AND_ALIAS)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }

    private fun criterion(index: Int, c: Criterion) {
        support.visitCriterion(index, c)
    }

    private class UpsertAliasManager(
        target: TableExpression<*>,
        excluded: TableExpression<*>,
    ) : AliasManager {
        private val aliasMap: Map<TableExpression<*>, String> = mapOf(
            target to "t0_",
            excluded to excluded.tableName(),
        )

        override val index: Int = 1

        override fun getAlias(expression: TableExpression<*>): String? {
            return aliasMap[expression]
        }
    }
}
