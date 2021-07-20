package org.komapper.dialect.mariadb

import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.Value
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.dialect.mariadb.MariaDbDialect

class MariaDbOffsetLimitStatementBuilder(
    private val dialect: MariaDbDialect,
    private val offset: Int,
    private val limit: Int
) : OffsetLimitStatementBuilder {

    private val buf = StatementBuffer()

    override fun build(): Statement {
        val offsetRequired = offset >= 0
        val limitRequired = limit > 0
        if (offsetRequired || limitRequired) {
            buf.append(" limit ")
            if (offsetRequired) {
                buf.bind(Value(offset, Int::class))
                buf.append(", ")
            }
            if (limitRequired) {
                buf.bind(Value(limit, Int::class))
            } else {
                buf.append(Long.MAX_VALUE.toString())
            }
        }
        return buf.toStatement()
    }
}
