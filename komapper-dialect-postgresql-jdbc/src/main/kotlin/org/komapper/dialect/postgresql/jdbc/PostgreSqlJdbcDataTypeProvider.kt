package org.komapper.dialect.postgresql.jdbc

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

class PostgreSqlJdbcDataTypeProvider(next: JdbcDataTypeProvider) :
    AbstractJdbcDataTypeProvider(next, DEFAULT_DATA_TYPES) {
    companion object {
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcArrayType("text[]"),
            JdbcBigDecimalType("numeric"),
            JdbcBigIntegerType("numeric"),
            JdbcBooleanType("boolean"),
            JdbcByteType("smallint"),
            JdbcByteArrayType("bytea"),
            JdbcClobStringType("text"),
            JdbcDoubleType("double precision"),
            JdbcFloatType("real"),
            JdbcInstantAsTimestampWithTimezoneType("timestamp with time zone"),
            JdbcIntType("integer"),
            JdbcLocalDateTimeType("timestamp"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("time"),
            JdbcLongType("bigint"),
            JdbcOffsetDateTimeType("timestamp with time zone"),
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
