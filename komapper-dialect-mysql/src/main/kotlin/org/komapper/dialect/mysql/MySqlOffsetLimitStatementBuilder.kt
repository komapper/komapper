package org.komapper.dialect.mysql

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.Value
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import kotlin.reflect.typeOf

class MySqlOffsetLimitStatementBuilder(
    private val dialect: BuilderDialect,
    private val offset: Int,
    private val limit: Int,
) : OffsetLimitStatementBuilder {

    private val buf = StatementBuffer()

    override fun build(): Statement {
        val offsetRequired = offset >= 0
        val limitRequired = limit > 0
        if (offsetRequired || limitRequired) {
            buf.append(" limit ")
            if (offsetRequired) {
                buf.bind(Value(offset, typeOf<Int>()))
                buf.append(", ")
            }
            if (limitRequired) {
                buf.bind(Value(limit, typeOf<Int>()))
            } else {
                buf.append(Long.MAX_VALUE.toString())
            }
        }
        return buf.toStatement()
    }
}
