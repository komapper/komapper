package integration.jdbc

import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class PairOfStringType : JdbcUserDefinedDataType<Pair<String, String>> {
    override val name: String = "varchar(500)"
    override val type: KType = typeOf<Pair<String, String>>()
    override val sqlType: JDBCType = JDBCType.VARCHAR

    override fun getValue(rs: ResultSet, index: Int): Pair<String, String>? {
        return rs.getString(index)?.let {
            val values = it.split(",")
            return Pair(values[0], values[1])
        }
    }

    override fun getValue(rs: ResultSet, columnLabel: String): Pair<String, String>? {
        return rs.getString(columnLabel)?.let {
            val values = it.split(",")
            return Pair(values[0], values[1])
        }
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Pair<String, String>) {
        return ps.setString(index, "${value.first},${value.second}")
    }

    override fun toString(value: Pair<String, String>): String {
        return "'${value.first},${value.second}'"
    }
}
