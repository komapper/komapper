package org.komapper.dialect.postgresql.jdbc

import org.komapper.core.type.BlobByteArray
import org.komapper.dialect.postgresql.PostgreSqlLiteral.toDoubleLiteral
import org.komapper.dialect.postgresql.PostgreSqlLiteral.toOffsetDateTimeLiteral
import org.komapper.jdbc.AbstractJdbcDataType
import org.komapper.jdbc.AbstractJdbcDataTypeProvider
import org.komapper.jdbc.JdbcArrayType
import org.komapper.jdbc.JdbcBigDecimalType
import org.komapper.jdbc.JdbcBigIntegerType
import org.komapper.jdbc.JdbcBooleanType
import org.komapper.jdbc.JdbcByteArrayType
import org.komapper.jdbc.JdbcByteType
import org.komapper.jdbc.JdbcClobStringType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDoubleType
import org.komapper.jdbc.JdbcFloatType
import org.komapper.jdbc.JdbcInstantAsTimestampWithTimezoneType
import org.komapper.jdbc.JdbcIntType
import org.komapper.jdbc.JdbcKotlinInstantAsTimestampWithTimezoneType
import org.komapper.jdbc.JdbcLocalDateTimeType
import org.komapper.jdbc.JdbcLocalDateType
import org.komapper.jdbc.JdbcLocalTimeType
import org.komapper.jdbc.JdbcLongType
import org.komapper.jdbc.JdbcOffsetDateTimeType
import org.komapper.jdbc.JdbcSQLXMLType
import org.komapper.jdbc.JdbcShortType
import org.komapper.jdbc.JdbcStringType
import org.komapper.jdbc.JdbcUByteType
import org.komapper.jdbc.JdbcUIntType
import org.komapper.jdbc.JdbcUShortType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.typeOf
import kotlin.time.ExperimentalTime

class PostgreSqlJdbcDataTypeProvider(next: JdbcDataTypeProvider) :
    AbstractJdbcDataTypeProvider(next, DEFAULT_DATA_TYPES) {
    companion object {
        @OptIn(ExperimentalTime::class)
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcArrayType("text[]"),
            JdbcBigDecimalType("numeric"),
            JdbcBigIntegerType("numeric"),
            JdbcBooleanType("boolean"),
            JdbcByteType("smallint"),
            JdbcByteArrayType("bytea"),
            PostgreSqlJdbcBlobByteArrayType,
            JdbcClobStringType("text"),
            JdbcDoubleType("double precision") { toDoubleLiteral(it) },
            JdbcFloatType("real"),
            JdbcInstantAsTimestampWithTimezoneType("timestamp with time zone"),
            JdbcIntType("integer"),
            JdbcKotlinInstantAsTimestampWithTimezoneType("timestamp with time zone"),
            JdbcLocalDateTimeType("timestamp"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("time"),
            JdbcLongType("bigint"),
            JdbcOffsetDateTimeType("timestamp with time zone") { toOffsetDateTimeLiteral(it) },
            JdbcShortType("smallint"),
            JdbcSQLXMLType("xml"),
            JdbcStringType("text"),
            JdbcUByteType("smallint"),
            JdbcUIntType("bigint"),
            JdbcUShortType("integer"),
            PostgreSqlJdbcUUIDType,
        )
    }
}

object PostgreSqlJdbcBlobByteArrayType :
    AbstractJdbcDataType<BlobByteArray>(typeOf<BlobByteArray>(), JDBCType.BINARY) {
    override val name: String = "bytea"
    override fun doGetValue(rs: ResultSet, index: Int): BlobByteArray? {
        return rs.getBytes(index)?.let { BlobByteArray(it) }
    }
    override fun doGetValue(rs: ResultSet, columnLabel: String): BlobByteArray? {
        return rs.getBytes(columnLabel)?.let { BlobByteArray(it) }
    }
    override fun doSetValue(ps: PreparedStatement, index: Int, value: BlobByteArray) {
        ps.setBytes(index, value.value)
    }
}
