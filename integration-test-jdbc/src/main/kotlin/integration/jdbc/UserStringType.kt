package integration.jdbc

import integration.core.UserString
import org.komapper.jdbc.spi.JdbcUserDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

class UserStringType : JdbcUserDataType<UserString> {
    override val name: String = "varchar(100)"

    override val klass: KClass<UserString> = UserString::class

    override val jdbcType: JDBCType = JDBCType.VARCHAR

    override fun getValue(rs: ResultSet, index: Int): UserString? {
        return rs.getString(index)?.let { UserString(it) }
    }

    override fun getValue(rs: ResultSet, columnLabel: String): UserString? {
        return rs.getString(columnLabel)?.let { UserString(it) }
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: UserString) {
        ps.setString(index, value.value)
    }

    override fun toString(value: UserString): String {
        return value.value
    }
}
