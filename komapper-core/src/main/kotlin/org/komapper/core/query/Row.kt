package org.komapper.core.query

import org.komapper.core.jdbc.Dialect
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.cast

class Row(
    private val dialect: Dialect,
    private val rs: ResultSet
) {

    fun asInt(columnLabel: String): Int {
        return dialect.getValue(rs, columnLabel, Int::class) as Int
    }

    fun asString(columnLabel: String): String {
        return dialect.getValue(rs, columnLabel, String::class) as String
    }

    fun <T : Any> asT(columnLabel: String, klass: KClass<T>): T {
        return dialect.getValue(rs, columnLabel, klass).let { klass.cast(it) }
    }
}
