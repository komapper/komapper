package integration.jdbc

import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class PairOfIntType : JdbcUserDefinedDataType<Pair<Int, Int>> {

    override val name: String = "varchar(500)"
    override val type: KType = typeOf<Pair<Int, Int>>()
    override val jdbcType: JDBCType = JDBCType.VARCHAR

    override fun getValue(rs: ResultSet, index: Int): Pair<Int, Int>? {
        return rs.getString(index)?.let {
            val values = it.split(",")
            return Pair(values[0].toInt(), values[1].toInt())
        }
    }

    override fun getValue(rs: ResultSet, columnLabel: String): Pair<Int, Int>? {
        return rs.getString(columnLabel)?.let {
            val values = it.split(",")
            return Pair(values[0].toInt(), values[1].toInt())
        }
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Pair<Int, Int>) {
        return ps.setString(index, "${value.first},${value.second}")
    }

    override fun toString(value: Pair<Int, Int>): String {
        return "'${value.first},${value.second}'"
    }
}
