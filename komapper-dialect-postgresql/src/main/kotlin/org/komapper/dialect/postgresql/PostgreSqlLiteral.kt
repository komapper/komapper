package org.komapper.dialect.postgresql

import org.komapper.core.DataType
import java.time.OffsetDateTime

object PostgreSqlLiteral {
    fun DataType.toDoubleLiteral(value: Double?): String {
        return "$value::double precision"
    }

    fun DataType.toOffsetDateTimeLiteral(value: OffsetDateTime?): String {
        val v = if (value == null) "null" else "'$value'"
        return "$v::timestamp with time zone"
    }
}
