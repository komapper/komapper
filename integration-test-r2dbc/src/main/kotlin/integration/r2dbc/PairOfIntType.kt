package integration.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class PairOfIntType : R2dbcUserDefinedDataType<Pair<Int, Int>> {
    override val name: String = "varchar(500)"
    override val type: KType = typeOf<Pair<Int, Int>>()
    override val r2dbcType: Class<String> = String::class.javaObjectType

    override fun getValue(row: Row, index: Int): Pair<Int, Int>? {
        return row.get(index, r2dbcType)?.let {
            val values = it.split(",")
            return Pair(values[0].toInt(), values[1].toInt())
        }
    }

    override fun getValue(row: Row, columnLabel: String): Pair<Int, Int>? {
        return row.get(columnLabel, r2dbcType)?.let {
            val values = it.split(",")
            return Pair(values[0].toInt(), values[1].toInt())
        }
    }

    override fun setValue(statement: Statement, index: Int, value: Pair<Int, Int>) {
        statement.bind(index, "${value.first},${value.second}")
    }

    override fun setValue(statement: Statement, name: String, value: Pair<Int, Int>) {
        statement.bind(name, "${value.first},${value.second}")
    }

    override fun toString(value: Pair<Int, Int>): String {
        return "'${value.first},${value.second}'"
    }
}
