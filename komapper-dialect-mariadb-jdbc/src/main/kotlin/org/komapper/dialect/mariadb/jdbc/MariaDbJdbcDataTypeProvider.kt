package org.komapper.dialect.mariadb.jdbc

import org.komapper.dialect.mariadb.MariaDbLiteral.toDoubleLiteral
import org.komapper.jdbc.AbstractJdbcDataTypeProvider
import org.komapper.jdbc.JdbcBigDecimalType
import org.komapper.jdbc.JdbcBigIntegerType
import org.komapper.jdbc.JdbcBlobByteArrayType
import org.komapper.jdbc.JdbcBlobType
import org.komapper.jdbc.JdbcBooleanType
import org.komapper.jdbc.JdbcByteArrayType
import org.komapper.jdbc.JdbcByteType
import org.komapper.jdbc.JdbcClobStringType
import org.komapper.jdbc.JdbcClobType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDoubleType
import org.komapper.jdbc.JdbcFloatType
import org.komapper.jdbc.JdbcInstantAsTimestampType
import org.komapper.jdbc.JdbcIntType
import org.komapper.jdbc.JdbcKotlinInstantAsTimestampType
import org.komapper.jdbc.JdbcLocalDateTimeType
import org.komapper.jdbc.JdbcLocalDateType
import org.komapper.jdbc.JdbcLocalTimeType
import org.komapper.jdbc.JdbcLongType
import org.komapper.jdbc.JdbcNClobType
import org.komapper.jdbc.JdbcSQLXMLType
import org.komapper.jdbc.JdbcShortType
import org.komapper.jdbc.JdbcStringType
import org.komapper.jdbc.JdbcUByteType
import org.komapper.jdbc.JdbcUIntType
import org.komapper.jdbc.JdbcUShortType
import kotlin.time.ExperimentalTime

class MariaDbJdbcDataTypeProvider(next: JdbcDataTypeProvider) : AbstractJdbcDataTypeProvider(next, DEFAULT_DATA_TYPES) {
    companion object {
        @OptIn(ExperimentalTime::class)
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcBigDecimalType("decimal"),
            JdbcBigIntegerType("decimal"),
            JdbcBlobType("blob"),
            JdbcBooleanType("bit(1)"),
            JdbcByteType("tinyint"),
            JdbcByteArrayType("varbinary(500)"),
            JdbcBlobByteArrayType("blob"),
            JdbcDoubleType("double precision") { toDoubleLiteral(it) },
            JdbcClobType("text"),
            JdbcClobStringType("text"),
            JdbcFloatType("real"),
            JdbcInstantAsTimestampType("timestamp"),
            JdbcIntType("integer"),
            JdbcKotlinInstantAsTimestampType("timestamp"),
            JdbcLocalDateTimeType("datetime(6)"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("time"),
            JdbcLongType("bigint"),
            JdbcNClobType("text"),
            JdbcShortType("smallint"),
            JdbcStringType("varchar(500)"),
            JdbcSQLXMLType("text"),
            JdbcUByteType("smallint"),
            JdbcUIntType("bigint"),
            JdbcUShortType("integer"),
        )
    }
}
