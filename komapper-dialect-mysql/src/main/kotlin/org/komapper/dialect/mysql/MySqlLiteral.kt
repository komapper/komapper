package org.komapper.dialect.mysql

import org.komapper.core.DataType
import java.time.OffsetDateTime

object MySqlLiteral {
    fun DataType.toDoubleLiteral(value: Double?): String {
        return value.toString()
    }

    fun DataType.toOffsetDateTimeLiteral(value: OffsetDateTime?): String {
        throw UnsupportedOperationException("OffsetDateTime literals are not supported.")
    }
}
