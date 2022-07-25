package integration.r2dbc

import integration.core.UserInt
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.r2dbc.spi.R2dbcUserDataType
import kotlin.reflect.KClass

class UserIntType : R2dbcUserDataType<UserInt> {

    override val name: String = "integer"

    override val klass: KClass<UserInt> = UserInt::class

    override val javaObjectType: Class<*> = Int::class.javaObjectType

    override fun getValue(row: Row, index: Int): UserInt? {
        return row.get(index, Int::class.javaObjectType)?.let { UserInt(it) }
    }

    override fun getValue(row: Row, columnLabel: String): UserInt? {
        return row.get(columnLabel, Int::class.javaObjectType)?.let { UserInt(it) }
    }

    override fun setValue(statement: Statement, index: Int, value: UserInt) {
        statement.bind(index, value.value)
    }

    override fun setValue(statement: Statement, name: String, value: UserInt) {
        statement.bind(name, value.value)
    }

    override fun toString(value: UserInt): String {
        return value.value.toString()
    }
}
