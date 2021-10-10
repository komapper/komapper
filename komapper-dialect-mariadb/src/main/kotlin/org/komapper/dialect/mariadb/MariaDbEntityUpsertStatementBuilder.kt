package org.komapper.dialect.mariadb

import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EmptyAliasManager
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.TableNameType
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel

class MariaDbEntityUpsertStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: MariaDbDialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : EntityUpsertStatementBuilder<ENTITY> {

    private val target = context.target
    private val aliasManager = EmptyAliasManager
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)

    override fun build(): Statement {
        if (context.assigned) {
            error("The 'EntityUpsertQueryBuilder#set' call is not supported. MariaDB cannot refer to the row to be inserted.")
        }
        buf.append("insert")
        if (context.duplicateKeyType == DuplicateKeyType.IGNORE) {
            buf.append(" ignore")
        }
        buf.append(" into ")
        table(target)
        buf.append(" (")
        for (
            p in target.properties().filter {
                it.idAssignment !is Assignment.AutoIncrement<ENTITY, *>
            }
        ) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values ")
        for (entity in entities) {
            buf.append("(")
            for (
                p in target.properties().filter {
                    it.idAssignment !is Assignment.AutoIncrement<ENTITY, *>
                }
            ) {
                buf.bind(p.toValue(entity))
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append("), ")
        }
        buf.cutBack(2)
        if (context.duplicateKeyType == DuplicateKeyType.UPDATE) {
            buf.append(" on duplicate key update ")
            for ((left) in context.assignmentMap) {
                column(left)
                buf.append(" = values(")
                column(left)
                buf.append("), ")
            }
            buf.cutBack(2)
        }
        return buf.toStatement()
    }

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_ONLY)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }
}
