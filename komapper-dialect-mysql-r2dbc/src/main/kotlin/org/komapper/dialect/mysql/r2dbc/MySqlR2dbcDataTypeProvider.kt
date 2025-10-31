package org.komapper.dialect.mysql.r2dbc

import org.komapper.dialect.mysql.MySqlLiteral.toDoubleLiteral
import org.komapper.dialect.mysql.MySqlLiteral.toOffsetDateTimeLiteral
import org.komapper.r2dbc.AbstractR2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcBigDecimalType
import org.komapper.r2dbc.R2dbcBigIntegerType
import org.komapper.r2dbc.R2dbcBlobByteArrayType
import org.komapper.r2dbc.R2dbcBlobType
import org.komapper.r2dbc.R2dbcBooleanType
import org.komapper.r2dbc.R2dbcByteArrayType
import org.komapper.r2dbc.R2dbcByteType
import org.komapper.r2dbc.R2dbcClobStringType
import org.komapper.r2dbc.R2dbcClobType
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDoubleType
import org.komapper.r2dbc.R2dbcFloatType
import org.komapper.r2dbc.R2dbcInstantAsTimestampType
import org.komapper.r2dbc.R2dbcIntType
import org.komapper.r2dbc.R2dbcKotlinInstantAsTimestampType
import org.komapper.r2dbc.R2dbcLocalDateTimeType
import org.komapper.r2dbc.R2dbcLocalDateType
import org.komapper.r2dbc.R2dbcLocalTimeType
import org.komapper.r2dbc.R2dbcLongType
import org.komapper.r2dbc.R2dbcOffsetDateTimeType
import org.komapper.r2dbc.R2dbcShortType
import org.komapper.r2dbc.R2dbcStringType
import org.komapper.r2dbc.R2dbcUByteType
import org.komapper.r2dbc.R2dbcUIntType
import org.komapper.r2dbc.R2dbcUShortType
import kotlin.time.ExperimentalTime

class MySqlR2dbcDataTypeProvider(next: R2dbcDataTypeProvider) :
    AbstractR2dbcDataTypeProvider(next, DEFAULT_DATA_TYPES) {
    companion object {
        @OptIn(ExperimentalTime::class)
        val DEFAULT_DATA_TYPES: List<R2dbcDataType<*>> = listOf(
            R2dbcBigDecimalType("decimal"),
            R2dbcBigIntegerType("decimal"),
            R2dbcBlobType("blob"),
            R2dbcBooleanType("bit(1)"),
            R2dbcByteType("tinyint"),
            R2dbcByteArrayType("varbinary(500)"),
            R2dbcBlobByteArrayType("blob"),
            R2dbcClobType("text"),
            R2dbcClobStringType("text"),
            R2dbcDoubleType("double precision") { toDoubleLiteral(it) },
            R2dbcFloatType("real"),
            R2dbcInstantAsTimestampType("timestamp"),
            R2dbcIntType("integer"),
            R2dbcKotlinInstantAsTimestampType("timestamp"),
            R2dbcLocalDateTimeType("datetime(6)"),
            R2dbcLocalDateType("date"),
            R2dbcLocalTimeType("time"),
            R2dbcLongType("bigint"),
            R2dbcOffsetDateTimeType("timestamp") { toOffsetDateTimeLiteral(it) },
            R2dbcShortType("smallint"),
            R2dbcStringType("varchar(500)"),
            R2dbcUByteType("smallint"),
            R2dbcUIntType("bigint"),
            R2dbcUShortType("integer"),
        )
    }
}
