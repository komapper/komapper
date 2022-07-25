package integration.r2dbc

import integration.core.UserDefinedString
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import kotlin.reflect.KClass

class UserDefinedStringType : R2dbcUserDefinedDataType<UserDefinedString> {
    override val name: String = "varchar(100)"

    override val klass: KClass<*> = UserDefinedString::class

    override val r2dbcType: Class<*> = String::class.javaObjectType

    override fun getValue(row: Row, index: Int): UserDefinedString? {
        return row.get(index, String::class.javaObjectType)?.let { UserDefinedString(it) }
    }

    override fun getValue(row: Row, columnLabel: String): UserDefinedString? {
        return row.get(columnLabel, String::class.javaObjectType)?.let { UserDefinedString(it) }
    }

    override fun setValue(statement: Statement, index: Int, value: UserDefinedString) {
        statement.bind(index, value.value)
    }

    override fun setValue(statement: Statement, name: String, value: UserDefinedString) {
        statement.bind(name, value.value)
    }

    override fun toString(value: UserDefinedString): String {
        return value.value
    }
}
