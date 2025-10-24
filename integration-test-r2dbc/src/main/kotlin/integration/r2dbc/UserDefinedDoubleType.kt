package integration.r2dbc

import integration.core.UserDefinedDouble
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import java.sql.JDBCType
import java.sql.SQLType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class UserDefinedDoubleType : R2dbcUserDefinedDataType<UserDefinedDouble> {
    override val name: String = "float8"

    override val type: KType = typeOf<UserDefinedDouble>()

    override val r2dbcType: Class<Double> = Double::class.javaObjectType

    override val sqlType: SQLType = JDBCType.DOUBLE

    override fun getValue(row: Row, index: Int): UserDefinedDouble? {
        return row.get(index, r2dbcType)?.let { UserDefinedDouble(it) }
    }

    override fun getValue(row: Row, columnLabel: String): UserDefinedDouble? {
        return row.get(columnLabel, r2dbcType)?.let { UserDefinedDouble(it) }
    }

    override fun setValue(
        statement: Statement,
        index: Int,
        value: UserDefinedDouble,
    ) {
        statement.bind(index, value.value)
    }

    override fun setValue(
        statement: Statement,
        name: String,
        value: UserDefinedDouble,
    ) {
        statement.bind(name, value.value)
    }

    override fun toString(value: UserDefinedDouble): String {
        return value.value.toString()
    }

    override fun toLiteral(value: UserDefinedDouble?): String {
        return "${value?.value}::float8"
    }
}
