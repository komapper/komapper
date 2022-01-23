package org.komapper.dialect.sqlserver

import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.Value
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder

class SqlServerOffsetLimitStatementBuilder(
    private val dialect: SqlServerDialect,
    private val offset: Int,
    private val limit: Int
) : OffsetLimitStatementBuilder {

    private val buf = StatementBuffer()

    override fun build(): Statement {
        val offsetRequired = offset >= 0
        val fetchRequired = limit > 0
        if (offsetRequired || fetchRequired) {
            buf.append(" offset ")
            buf.bind(Value(if (offset > 0) offset else 0, Int::class))
            buf.append(" rows")
            if (limit > 0) {
                buf.append(" fetch first ")
                buf.bind(Value(limit, Int::class))
                buf.append(" rows only")
            }
        }
        return buf.toStatement()
    }
}
