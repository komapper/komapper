package org.komapper.dialect.sqlserver.r2dbc

import org.komapper.r2dbc.AbstractR2dbcDataType

object R2dbcSqlServerBooleanType : AbstractR2dbcDataType<Boolean>(Boolean::class) {
    override val name: String = "bit"

    override fun convert(value: Any): Boolean {
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() == 1
            is String -> value.toBoolean()
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }

    override fun doToString(value: Boolean): String {
        return "'" + value.toString().uppercase() + "'"
    }
}
