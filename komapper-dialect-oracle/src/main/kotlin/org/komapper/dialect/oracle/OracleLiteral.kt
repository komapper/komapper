package org.komapper.dialect.oracle

import org.komapper.core.DataType
import java.time.OffsetDateTime

object OracleLiteral {
    fun DataType.toDoubleLiteral(value: Double?): String {
        return "cast($value as float)"
    }

    fun DataType.toOffsetDateTimeLiteral(value: OffsetDateTime?): String {
        val v = if (value == null) "null" else "'$value'"
        return "to_timestamp_tz($v, 'YYYY-MM-DD\"T\"HH24:MI:SSTZH:TZM')"
    }
}
