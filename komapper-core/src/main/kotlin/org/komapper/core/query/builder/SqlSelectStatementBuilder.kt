package org.komapper.core.query.builder

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.query.context.SqlSelectContext

internal class SqlSelectStatementBuilder<ENTITY>(
    val config: DatabaseConfig,
    val context: SqlSelectContext<ENTITY>,
    aliasManager: AliasManager = AliasManager(context)
) {
    private val buf = StatementBuffer(config.dialect::formatValue)
    private val support = SelectStatementBuilderSupport(config, context, aliasManager, buf)

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
