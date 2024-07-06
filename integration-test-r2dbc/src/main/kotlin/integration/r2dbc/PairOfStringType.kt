package integration.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class PairOfStringType : R2dbcUserDefinedDataType<Pair<String, String>> {

    override val name: String = "varchar(500)"
    override val type: KType = typeOf<Pair<String, String>>()
    override val r2dbcType: Class<String> = String::class.javaObjectType

    override fun getValue(row: Row, index: Int): Pair<String, String>? {
        return row.get(index, r2dbcType)?.let {
            val values = it.split(",")
            return Pair(values[0], values[1])
        }
    }

    override fun getValue(row: Row, columnLabel: String): Pair<String, String>? {
        return row.get(columnLabel, r2dbcType)?.let {
            val values = it.split(",")
            return Pair(values[0], values[1])
        }
    }

    override fun setValue(statement: Statement, index: Int, value: Pair<String, String>) {
        statement.bind(index, "${value.first},${value.second}")
    }

    override fun setValue(statement: Statement, name: String, value: Pair<String, String>) {
        statement.bind(name, "${value.first},${value.second}")
    }

    override fun toString(value: Pair<String, String>): String {
        return "'${value.first},${value.second}'"
    }
}
