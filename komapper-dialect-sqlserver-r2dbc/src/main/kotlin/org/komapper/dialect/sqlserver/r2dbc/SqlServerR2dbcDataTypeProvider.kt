package org.komapper.dialect.sqlserver.r2dbc

import org.komapper.r2dbc.AbstractR2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcBigDecimalType
import org.komapper.r2dbc.R2dbcBigIntegerType
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
import org.komapper.r2dbc.R2dbcInstantAsTimestampWithTimezoneType
import org.komapper.r2dbc.R2dbcIntType
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

class SqlServerR2dbcDataTypeProvider(next: R2dbcDataTypeProvider) :
    AbstractR2dbcDataTypeProvider(next, DEFAULT_DATA_TYPES) {
    companion object {
        val DEFAULT_DATA_TYPES: List<R2dbcDataType<*>> = listOf(
            R2dbcBigDecimalType("decimal"),
            R2dbcBigIntegerType("decimal"),
            R2dbcBlobType("varbinary(max)"),
            R2dbcBooleanType("bit"),
            R2dbcByteType("smallint"),
            R2dbcByteArrayType("varbinary(1000)"),
            R2dbcClobType("text"),
            R2dbcClobStringType("text"),
            R2dbcDoubleType("real"),
            R2dbcFloatType("float"),
            R2dbcInstantAsTimestampWithTimezoneType("datetimeoffset"),
            R2dbcIntType("int"),
            R2dbcLocalDateTimeType("datetime2"),
            R2dbcLocalDateType("date"),
            R2dbcLocalTimeType("time"),
            R2dbcLongType("bigint"),
            R2dbcOffsetDateTimeType("datetimeoffset"),
            R2dbcShortType("smallint"),
            R2dbcStringType("varchar(1000)"),
            R2dbcUByteType("smallint"),
            R2dbcUIntType("bigint"),
            R2dbcUShortType("int"),
            SqlServerR2dbcBooleanType,
        )
    }
}
