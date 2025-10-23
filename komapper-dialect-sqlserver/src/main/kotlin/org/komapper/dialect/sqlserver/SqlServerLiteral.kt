package org.komapper.dialect.sqlserver

import org.komapper.core.DataType
import java.time.OffsetDateTime

object SqlServerLiteral {
    fun DataType.toDoubleLiteral(value: Double?): String {
        return "cast($value as float)"
    }

    fun DataType.toOffsetDateTimeLiteral(value: OffsetDateTime?): String {
        val v = if (value == null) "null" else "'$value'"
        return "cast($v as datetimeoffset(6))"
    }
}
