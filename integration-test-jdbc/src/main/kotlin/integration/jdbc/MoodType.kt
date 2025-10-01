package integration.jdbc

import integration.core.enumclass.Mood
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class MoodType : JdbcUserDefinedDataType<Mood> {
    override val name: String = "mood"

    override val type: KType = typeOf<Mood>()

    override val sqlType: JDBCType = JDBCType.OTHER

    override fun getValue(rs: ResultSet, index: Int): Mood? {
        return rs.getString(index)?.let { Mood.valueOf(it) }
    }

    override fun getValue(rs: ResultSet, columnLabel: String): Mood? {
        return rs.getString(columnLabel)?.let { Mood.valueOf(it) }
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Mood) {
        ps.setObject(index, value.name, sqlType.vendorTypeNumber)
    }

    override fun toString(value: Mood): String {
        return "'${value.name}'"
    }
}
