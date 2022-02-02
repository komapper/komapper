package org.komapper.dialect.sqlserver.r2dbc

import org.komapper.r2dbc.R2dbcAbstractDataType

object R2dbcSqlServerBooleanType : R2dbcAbstractDataType<Boolean>(Boolean::class) {
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
