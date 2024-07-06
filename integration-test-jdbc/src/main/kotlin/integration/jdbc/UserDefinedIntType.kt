package integration.jdbc

import integration.core.UserDefinedInt
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class UserDefinedIntType : JdbcUserDefinedDataType<UserDefinedInt> {
    override val name: String = "integer"

    override val type: KType = typeOf<UserDefinedInt>()

    override val jdbcType: JDBCType = JDBCType.INTEGER

    override fun getValue(rs: ResultSet, index: Int): UserDefinedInt {
        return UserDefinedInt(rs.getInt(index))
    }

    override fun getValue(rs: ResultSet, columnLabel: String): UserDefinedInt {
        return UserDefinedInt(rs.getInt(columnLabel))
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: UserDefinedInt) {
        ps.setInt(index, value.value)
    }

    override fun toString(value: UserDefinedInt): String {
        return value.value.toString()
    }
}
