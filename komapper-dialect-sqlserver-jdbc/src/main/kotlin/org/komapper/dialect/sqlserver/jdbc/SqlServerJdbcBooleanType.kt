package org.komapper.dialect.sqlserver.jdbc

import org.komapper.jdbc.AbstractJdbcDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.typeOf

object SqlServerJdbcBooleanType : AbstractJdbcDataType<Boolean>(typeOf<Boolean>(), JDBCType.BOOLEAN) {
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
