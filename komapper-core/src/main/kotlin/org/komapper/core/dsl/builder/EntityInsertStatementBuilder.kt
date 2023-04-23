package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.getNonAutoIncrementProperties

interface EntityInsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    fun build(): Statement
}

class DefaultEntityInsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityInsertStatementBuilder<ENTITY, ID, META> {

    override fun build(): Statement {
        val buf = StatementBuffer()
        buf.append(buildInsertInto())
        buf.append(" ")
        buf.append(buildValues())
        return buf.toStatement()
    }

    fun buildInsertInto(): Statement {
        val target = context.target
        val properties = target.getNonAutoIncrementProperties()
        return with(StatementBuffer()) {
            append("insert into ")
            table(target)
            append(" (")
            for (p in properties) {
                column(p)
                append(", ")
            }
            cutBack(2)
            append(")")
            toStatement()
        }
    }

    fun buildValues(): Statement {
        val target = context.target
        val properties = target.getNonAutoIncrementProperties()
        return with(StatementBuffer()) {
            append("values ")
            for (entity in entities) {
                append("(")
                for (p in properties) {
                    bind(p.toValue(entity))
                    append(", ")
                }
                cutBack(2)
                append("), ")
            }
            cutBack(2)
            toStatement()
        }
    }

    private fun StatementBuffer.table(metamodel: EntityMetamodel<*, *, *>) {
        val name = metamodel.getCanonicalTableName(dialect::enquote)
        append(name)
    }

    private fun StatementBuffer.column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        append(name)
    }
}
