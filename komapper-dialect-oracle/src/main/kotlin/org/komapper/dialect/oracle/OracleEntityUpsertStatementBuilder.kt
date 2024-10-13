package org.komapper.dialect.oracle

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.AliasManager
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.TableNameType
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

internal class OracleEntityUpsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    entities: List<ENTITY>,
) : EntityUpsertStatementBuilder<ENTITY> {
    private val target = context.target
    private val excluded = context.excluded
    private val aliasManager = UpsertAliasManager(target, excluded)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf, context.insertContext.options.escapeSequence)
    private val sourceStatementBuilder = SourceStatementBuilder(dialect, context, entities)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val keys = context.keys.ifEmpty { context.target.idProperties() }

        @Suppress("NAME_SHADOWING")
        val assignments = assignments.filter { (left, _) -> left !in keys }
        buf.append("merge into ")
        table(target, TableNameType.NAME_AND_ALIAS)
        buf.append(" using (")
        buf.append(sourceStatementBuilder.build())
        buf.append(") ")
        table(excluded, TableNameType.ALIAS_ONLY)
        buf.append(" on (")
        val excludedPropertyMap = excluded.properties().associateBy { it.name }
        for (key in keys) {
            column(key)
            buf.append(" = ")
            column(excludedPropertyMap[key.name]!!)
            buf.append(" and ")
        }
        buf.cutBack(5)
        buf.append(")")
        if (context.duplicateKeyType == DuplicateKeyType.UPDATE && assignments.isNotEmpty()) {
            buf.append(" when matched then update set ")
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
        buf.append(" when not matched then insert (")
        for (p in target.getNonAutoIncrementProperties()) {
            columnWithoutAlias(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values (")
        for (p in excluded.getNonAutoIncrementProperties()) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        return buf.toStatement()
    }

    private fun table(expression: TableExpression<*>, tableNameType: TableNameType) {
        support.visitTableExpression(expression, tableNameType)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        support.visitColumnExpression(expression)
    }

    private fun columnWithoutAlias(expression: ColumnExpression<*, *>) {
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
        val target: TableExpression<*>,
        val excluded: TableExpression<*>,
    ) : AliasManager {
        override val index: Int = 0

        override fun getAlias(expression: TableExpression<*>): String {
            return when (expression) {
                target -> "t"
                excluded -> excluded.tableName()
                else -> ""
            }
        }
    }

    private class SourceStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
        val dialect: BuilderDialect,
        val context: EntityUpsertContext<ENTITY, ID, META>,
        val entities: List<ENTITY>,
    ) {
        private val buf = StatementBuffer()

        fun build(): Statement {
            val properties = context.target.properties()
            for (entity in entities) {
                buf.append("select ")
                for (p in properties) {
                    buf.bind(p.toValue(entity))
                    buf.append(" as ")
                    column(p)
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append(" from dual")
                buf.append(" union all ")
            }
            buf.cutBack(11)
            return buf.toStatement()
        }

        private fun column(expression: ColumnExpression<*, *>) {
            val name = expression.getCanonicalColumnName(dialect::enquote)
            buf.append(name)
        }
    }
}
