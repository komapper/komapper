package org.komapper.dialect.oracle.jdbc

import oracle.jdbc.OraclePreparedStatement
import org.komapper.jdbc.AbstractJdbcDataType
import org.komapper.jdbc.JdbcDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet

class OracleJdbcDataType<T : Any>(private val jdbcDataType: JdbcDataType<T>) : JdbcDataType<T> by jdbcDataType {
    override fun registerReturnParameter(ps: PreparedStatement, index: Int) {
        val ops = ps.unwrap(OraclePreparedStatement::class.java)
        ops.registerReturnParameter(index, jdbcType.vendorTypeNumber)
    }
}

object OracleJdbcBooleanType : AbstractJdbcDataType<Boolean>(Boolean::class, JDBCType.INTEGER) {
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

// TODO
fun <T : Any> wrap(jdbcDataType: JdbcDataType<T>): OracleJdbcDataType<T> {
    return if (jdbcDataType is OracleJdbcDataType<T>) {
        jdbcDataType
    } else {
        OracleJdbcDataType(jdbcDataType)
    }
}
