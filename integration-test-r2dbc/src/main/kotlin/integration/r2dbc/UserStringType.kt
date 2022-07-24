package integration.r2dbc

import integration.core.UserString
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.r2dbc.spi.R2dbcUserDataType
import kotlin.reflect.KClass

class UserStringType : R2dbcUserDataType<UserString> {
    override val name: String = "varchar(100)"

    override val klass: KClass<*> = UserString::class

    override val javaObjectType: Class<*> = String::class.javaObjectType

    override fun getValue(row: Row, index: Int): UserString? {
        return row.get(index, String::class.javaObjectType)?.let { UserString(it) }
    }

    override fun getValue(row: Row, columnLabel: String): UserString? {
        return row.get(columnLabel, String::class.javaObjectType)?.let { UserString(it) }
    }

    override fun setValue(statement: Statement, index: Int, value: UserString) {
        statement.bind(index, value.value)
    }

    override fun setValue(statement: Statement, name: String, value: UserString) {
        statement.bind(name, value.value)
    }

    override fun toString(value: UserString): String {
        return value.value
    }
}
