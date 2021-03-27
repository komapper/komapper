package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.EntitySelectContext

internal class EntitySelectStatementBuilder<ENTITY>(
    val dialect: Dialect,
    val context: EntitySelectContext<ENTITY>,
    aliasManager: AliasManager = AliasManager(context)
) {
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = SelectStatementBuilderSupport(dialect, context, aliasManager, buf)

    fun build(): Statement {
        selectClause()
        fromClause()
        whereClause()
        orderByClause()
        offsetLimitClause()
        forUpdateClause()
        return buf.toStatement()
    }

    private fun selectClause() {
        support.selectClause()
    }

    private fun fromClause() {
        support.fromClause()
    }

    private fun whereClause() {
        support.whereClause()
    }

    private fun orderByClause() {
        support.orderByClause()
    }

    private fun offsetLimitClause() {
        support.offsetLimitClause()
    }

    private fun forUpdateClause() {
        support.forUpdateClause()
    }
}
