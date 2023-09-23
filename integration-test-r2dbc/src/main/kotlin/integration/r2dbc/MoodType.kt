package integration.r2dbc

import integration.core.enumclass.Mood
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import kotlin.reflect.KClass

class MoodType : R2dbcUserDefinedDataType<Mood> {
    override val name: String = "mood"

    override val klass: KClass<Mood> = Mood::class

    override val r2dbcType: Class<Mood> = Mood::class.javaObjectType

    override fun getValue(row: Row, index: Int): Mood? {
        return row.get(index, r2dbcType)
    }

    override fun getValue(row: Row, columnLabel: String): Mood? {
        return row.get(columnLabel, r2dbcType)
    }

    override fun setValue(statement: Statement, index: Int, value: Mood) {
        statement.bind(index, value)
    }

    override fun setValue(statement: Statement, name: String, value: Mood) {
        statement.bind(name, value)
    }

    override fun toString(value: Mood): String {
        return "'${value.name}'"
    }
}
