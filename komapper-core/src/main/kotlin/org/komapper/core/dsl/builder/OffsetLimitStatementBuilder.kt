package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value

interface OffsetLimitStatementBuilder {
    fun build(): Statement
}

internal class OffsetLimitStatementBuilderImpl(
    private val dialect: Dialect,
    private val offset: Int,
    private val limit: Int
) : OffsetLimitStatementBuilder {

    private val buf = StatementBuffer(dialect::formatValue)

    override fun build(): Statement {
        if (offset >= 0) {
            buf.append(" offset ")
            buf.bind(Value(offset, Int::class))
            buf.append(" rows")
        }
        if (limit > 0) {
            buf.append(" fetch first ")
            buf.bind(Value(limit, Int::class))
            buf.append(" rows only")
        }
        return buf.toStatement()
    }
}
