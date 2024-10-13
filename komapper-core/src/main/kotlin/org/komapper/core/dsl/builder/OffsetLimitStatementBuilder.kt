package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.Value
import kotlin.reflect.typeOf

interface OffsetLimitStatementBuilder {
    fun build(): Statement
}

class OffsetLimitStatementBuilderImpl(
    @Suppress("unused") private val dialect: BuilderDialect,
    private val offset: Int,
    private val limit: Int,
) : OffsetLimitStatementBuilder {
    private val buf = StatementBuffer()

    override fun build(): Statement {
        if (offset >= 0) {
            buf.append(" offset ")
            buf.bind(Value(offset, typeOf<Int>()))
            buf.append(" rows")
        }
        if (limit > 0) {
            buf.append(" fetch first ")
            buf.bind(Value(limit, typeOf<Int>()))
            buf.append(" rows only")
        }
        return buf.toStatement()
    }
}
