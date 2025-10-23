package org.komapper.dialect.mariadb

import org.komapper.core.DataType

object MariaDbLiteral {
    fun DataType.toDoubleLiteral(value: Double?): String {
        return value.toString()
    }
}
