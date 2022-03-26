package org.komapper.dialect.oracle.jdbc

import org.komapper.jdbc.AbstractJdbcDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet

object JdbcOracleBooleanType : AbstractJdbcDataType<Boolean>(Boolean::class, JDBCType.BOOLEAN) {
    override val name: String = "number(1, 0)"

    override fun doGetValue(rs: ResultSet, index: Int): Boolean {
        return toBoolean(rs.getInt(index))
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Boolean {
        return toBoolean(rs.getInt(columnLabel))
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Boolean) {
        ps.setInt(index, toInt(value))
    }

    override fun doToString(value: Boolean): String {
        return toInt(value).toString()
    }

    private fun toBoolean(value: Int): Boolean {
        return value == 1
    }

    private fun toInt(value: Boolean): Int {
        return if (value) 1 else 0
    }
}
