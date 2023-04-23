package org.komapper.dialect.oracle

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultEntityInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.getNonAutoIncrementProperties

class OracleEntityInsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) :
    EntityInsertStatementBuilder<ENTITY, ID, META> {

    init {
        require(entities.isNotEmpty()) { "entities must not be empty." }
    }

    override fun build(): Statement {
        return if (entities.size == 1) {
            buildForSingleEntity()
        } else {
            buildForMultipleEntities()
        }
    }

    private fun buildForSingleEntity(): Statement {
        val builder = DefaultEntityInsertStatementBuilder(dialect, context, entities)
        val support = OracleStatementBuilderSupport(dialect, context)
        return with(StatementBuffer()) {
            append(builder.build())
            val returningStatement = support.buildReturning()
            if (returningStatement.parts.isNotEmpty()) {
                append(" ")
                append(returningStatement)
            }
            toStatement()
        }
    }

    private fun buildForMultipleEntities(): Statement {
        val target = context.target
        val properties = target.getNonAutoIncrementProperties()
        return with(StatementBuffer()) {
            append("insert all ")
            for (entity in entities) {
                append("into ")
                table(target)
                append(" (")
                for (p in properties) {
                    column(p)
                    append(", ")
                }
                cutBack(2)
                append(") values (")
                for (p in properties) {
                    bind(p.toValue(entity))
                    append(", ")
                }
                cutBack(2)
                append(") ")
            }
            append("select 1 from dual")
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
