package org.komapper.dialect.oracle.r2dbc

import org.komapper.r2dbc.AbstractR2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcBigDecimalType
import org.komapper.r2dbc.R2dbcBigIntegerType
import org.komapper.r2dbc.R2dbcBlobType
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

class OracleR2dbcDataTypeProvider(next: R2dbcDataTypeProvider) :
    AbstractR2dbcDataTypeProvider(next, DEFAULT_DATA_TYPES) {
    companion object {
        val DEFAULT_DATA_TYPES: List<R2dbcDataType<*>> = listOf(
            R2dbcBigDecimalType("decimal"),
            R2dbcBigIntegerType("decimal"),
            R2dbcBlobType("blob"),
            R2dbcByteType("integer"),
            R2dbcByteArrayType("raw(500)"),
            R2dbcClobType("clob"),
            R2dbcClobStringType("clob"),
            R2dbcDoubleType("float"),
            R2dbcFloatType("float"),
            R2dbcInstantAsTimestampWithTimezoneType("timestamp with time zone"),
            R2dbcIntType("integer"),
            R2dbcLocalDateTimeType("timestamp"),
            R2dbcLocalDateType("date"),
            R2dbcLocalTimeType("date"),
            R2dbcLongType("integer"),
            R2dbcOffsetDateTimeType("timestamp with time zone"),
            R2dbcShortType("integer"),
            R2dbcStringType("varchar2(1000)"),
            R2dbcUByteType("integer"),
            R2dbcUIntType("integer"),
            R2dbcUShortType("integer"),
            OracleR2dbcBooleanType,
            OracleR2dbcDurationType,
            OracleR2dbcPeriodType,
        )
    }
}
