package integration.jdbc

import integration.core.UserInt
import org.komapper.jdbc.spi.JdbcUserDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

class UserIntType : JdbcUserDataType<UserInt> {
    override val name: String = "integer"

    override val klass: KClass<UserInt> = UserInt::class

    override val jdbcType: JDBCType = JDBCType.INTEGER

    override fun getValue(rs: ResultSet, index: Int): UserInt {
        return UserInt(rs.getInt(index))
    }

    override fun getValue(rs: ResultSet, columnLabel: String): UserInt {
        return UserInt(rs.getInt(columnLabel))
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: UserInt) {
        ps.setInt(index, value.value)
    }

    override fun toString(value: UserInt): String {
        return value.value.toString()
    }
}
