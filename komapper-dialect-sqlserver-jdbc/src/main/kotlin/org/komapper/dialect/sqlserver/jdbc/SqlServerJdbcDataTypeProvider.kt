package org.komapper.dialect.sqlserver.jdbc

import org.komapper.dialect.sqlserver.SqlServerLiteral.toDoubleLiteral
import org.komapper.dialect.sqlserver.SqlServerLiteral.toOffsetDateTimeLiteral
import org.komapper.jdbc.AbstractJdbcDataTypeProvider
import org.komapper.jdbc.JdbcBigDecimalType
import org.komapper.jdbc.JdbcBigIntegerType
import org.komapper.jdbc.JdbcBlobType
import org.komapper.jdbc.JdbcByteArrayType
import org.komapper.jdbc.JdbcByteType
import org.komapper.jdbc.JdbcClobStringType
import org.komapper.jdbc.JdbcClobType
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
import org.komapper.jdbc.JdbcNClobType
import org.komapper.jdbc.JdbcOffsetDateTimeType
import org.komapper.jdbc.JdbcSQLXMLType
import org.komapper.jdbc.JdbcShortType
import org.komapper.jdbc.JdbcStringType
import org.komapper.jdbc.JdbcUByteType
import org.komapper.jdbc.JdbcUIntType
import org.komapper.jdbc.JdbcUShortType
import kotlin.time.ExperimentalTime

class SqlServerJdbcDataTypeProvider(next: JdbcDataTypeProvider) :
    AbstractJdbcDataTypeProvider(next, DEFAULT_DATA_TYPES) {
    companion object {
        @OptIn(ExperimentalTime::class)
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcBigDecimalType("decimal"),
            JdbcBigIntegerType("decimal"),
            JdbcBlobType("varbinary(max)"),
            JdbcByteType("tinyint"),
            JdbcByteArrayType("varbinary(1000)"),
            JdbcClobType("text"),
            JdbcClobStringType("text"),
            JdbcDoubleType("float") { toDoubleLiteral(it) },
            JdbcFloatType("real"),
            JdbcInstantAsTimestampWithTimezoneType("datetimeoffset"),
            JdbcIntType("int"),
            JdbcKotlinInstantAsTimestampWithTimezoneType("datetimeoffset"),
            JdbcLocalDateTimeType("datetime2"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("time"),
            JdbcLongType("bigint"),
            JdbcNClobType("ntext"),
            JdbcOffsetDateTimeType("datetimeoffset") { toOffsetDateTimeLiteral(it) },
            JdbcShortType("smallint"),
            JdbcSQLXMLType("xml"),
            JdbcStringType("varchar(1000)"),
            JdbcUByteType("smallint"),
            JdbcUIntType("bigint"),
            JdbcUShortType("int"),
            SqlServerJdbcBooleanType,
        )
    }
}
