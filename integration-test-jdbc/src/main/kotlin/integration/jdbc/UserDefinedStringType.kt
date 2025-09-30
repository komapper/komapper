package integration.jdbc

import integration.core.UserDefinedString
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class UserDefinedStringType : JdbcUserDefinedDataType<UserDefinedString> {
    override val name: String = "varchar(100)"

    override val type: KType = typeOf<UserDefinedString>()

    override val sqlType: JDBCType = JDBCType.VARCHAR

    override fun getValue(rs: ResultSet, index: Int): UserDefinedString? {
        return rs.getString(index)?.let { UserDefinedString(it) }
    }

    override fun getValue(rs: ResultSet, columnLabel: String): UserDefinedString? {
        return rs.getString(columnLabel)?.let { UserDefinedString(it) }
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: UserDefinedString) {
        ps.setString(index, value.value)
    }

    override fun toString(value: UserDefinedString): String {
        return value.value
    }
}
