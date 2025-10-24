package integration.jdbc

import integration.core.UserDefinedDouble
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class UserDefinedDoubleType : JdbcUserDefinedDataType<UserDefinedDouble> {
    override val name: String = "float8"

    override val type: KType = typeOf<UserDefinedDouble>()

    override val sqlType: SQLType = JDBCType.DOUBLE

    override fun getValue(rs: ResultSet, index: Int): UserDefinedDouble {
        return UserDefinedDouble(rs.getDouble(index))
    }

    override fun getValue(rs: ResultSet, columnLabel: String): UserDefinedDouble {
        return UserDefinedDouble(rs.getDouble(columnLabel))
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: UserDefinedDouble) {
        ps.setDouble(index, value.value)
    }

    override fun toString(value: UserDefinedDouble): String {
        return value.value.toString()
    }

    override fun toLiteral(value: UserDefinedDouble?): String {
        return "${value?.value}::float8"
    }
}
