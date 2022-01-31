package org.komapper.dialect.sqlserver.jdbc

import org.komapper.jdbc.AbstractDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet

object SqlServerBooleanType : AbstractDataType<Boolean>(Boolean::class, JDBCType.BOOLEAN) {
    override val name: String = "bit"

    override fun doGetValue(rs: ResultSet, index: Int): Boolean {
        return rs.getBoolean(index)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Boolean {
        return rs.getBoolean(columnLabel)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Boolean) {
        ps.setBoolean(index, value)
    }

    override fun doToString(value: Boolean): String {
        return "'" + value.toString().uppercase() + "'"
    }
}
