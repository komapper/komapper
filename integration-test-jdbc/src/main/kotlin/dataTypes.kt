package integration

import org.komapper.jdbc.DataType
import org.komapper.jdbc.StringType
import org.postgresql.util.PGobject
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

class PostgreSqlJsonType : DataType<Json> {
    private val stringType = StringType("")
    override val klass: KClass<Json> = Json::class
    override val name: String = "jsonb"
    override val jdbcType: JDBCType = stringType.jdbcType

    override fun getValue(rs: ResultSet, index: Int): Json? {
        val data = stringType.getValue(rs, index)
        return if (data == null) null else Json(data)
    }

    override fun getValue(rs: ResultSet, columnLabel: String): Json? {
        val data = stringType.getValue(rs, columnLabel)
        return if (data == null) null else Json(data)
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Json?) {
        val obj = PGobject()
        obj.value = value?.data
        obj.type = "jsonb"
        ps.setObject(index, obj)
    }

    override fun toString(value: Json?): String {
        return stringType.toString(value?.data)
    }
}
