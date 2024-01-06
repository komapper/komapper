package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface EntityUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun build(): Statement
}

class DefaultEntityUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpdateStatementBuilder<ENTITY, ID, META> {

    override fun build(): Statement {
        val buf = StatementBuffer()
        buf.append(buildUpdateSet())
        buf.append(" ")
        buf.append(buildWhere())
        return buf.toStatement()
    }

    fun buildUpdateSet(): Statement {
        val target = context.target
        val versionProperty = target.versionProperty()
        return with(StatementBuffer()) {
            val support = BuilderSupport(dialect, EmptyAliasManager, this)
            append("update ")
            table(target, support)
            append(" set ")
            val targetProperties = context.getTargetProperties()
            require(targetProperties.isNotEmpty()) {
                "There are no entity properties to specify in the SET clause of the UPDATE statement"
            }
            for (p in targetProperties) {
                column(p)
                append(" = ")
                bind(p.toValue(entity))
                if (p == versionProperty) {
                    append(" + 1")
                }
                append(", ")
            }
            cutBack(2)
            toStatement()
        }
    }

    fun buildWhere(): Statement {
        val target = context.target
        val idProperties = target.idProperties()
        val versionProperty = target.versionProperty()
        val criteria = context.getWhereCriteria()
        val versionRequired = versionProperty != null && !context.options.disableOptimisticLock
        return with(StatementBuffer()) {
            val support = BuilderSupport(dialect, EmptyAliasManager, this)
            if (criteria.isNotEmpty() || idProperties.isNotEmpty() || versionRequired) {
                append("where ")
                for ((index, criterion) in criteria.withIndex()) {
                    criterion(index, criterion, support)
                    append(" and ")
                }
                if (idProperties.isNotEmpty()) {
                    for (p in idProperties) {
                        column(p)
                        append(" = ")
                        bind(p.toValue(entity))
                        append(" and ")
                    }
                    if (!versionRequired) {
                        cutBack(5)
                    }
                }
                if (versionRequired) {
                    checkNotNull(versionProperty)
                    column(versionProperty)
                    append(" = ")
                    bind(versionProperty.toValue(entity))
                }
            }
            toStatement()
        }
    }

    private fun table(expression: TableExpression<*>, support: BuilderSupport) {
        support.visitTableExpression(expression, TableNameType.NAME_ONLY)
    }

    private fun StatementBuffer.column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        append(name)
    }

    private fun criterion(index: Int, c: Criterion, support: BuilderSupport) {
        support.visitCriterion(index, c)
    }
}
