package org.komapper.dialect.oracle

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.getNonAutoIncrementProperties

class OracleEntityInsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) :
    EntityInsertStatementBuilder<ENTITY, ID, META> {

    init {
        require(entities.isNotEmpty()) { "entities must not be empty." }
    }

    private val buf = StatementBuffer()

    override fun build(): Statement {
        val target = context.target
        val properties = target.getNonAutoIncrementProperties()
        buf.append("insert all ")
        for (entity in entities) {
            buf.append("into ")
            table(target)
            buf.append(" (")
            for (p in properties) {
                column(p)
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append(") values (")
            for (p in properties) {
                buf.bind(p.toValue(entity))
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append(") ")
        }
        buf.append("select 1 from dual")
        return buf.toStatement()
    }

    private fun table(metamodel: EntityMetamodel<*, *, *>) {
        val name = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append(name)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }
}
