package integration.r2dbc

import integration.core.UserDefinedInt
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import java.sql.JDBCType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class UserDefinedIntType : R2dbcUserDefinedDataType<UserDefinedInt> {
    override val name: String = "integer"

    override val type: KType = typeOf<UserDefinedInt>()

    override val r2dbcType: Class<*> = Int::class.javaObjectType

    override val sqlType: JDBCType = JDBCType.VARCHAR

    override fun getValue(row: Row, index: Int): UserDefinedInt? {
        return row.get(index, Int::class.javaObjectType)?.let { UserDefinedInt(it) }
    }

    override fun getValue(row: Row, columnLabel: String): UserDefinedInt? {
        return row.get(columnLabel, Int::class.javaObjectType)?.let { UserDefinedInt(it) }
    }

    override fun setValue(statement: Statement, index: Int, value: UserDefinedInt) {
        statement.bind(index, value.value)
    }

    override fun setValue(statement: Statement, name: String, value: UserDefinedInt) {
        statement.bind(name, value.value)
    }

    override fun toString(value: UserDefinedInt): String {
        return value.value.toString()
    }
}
