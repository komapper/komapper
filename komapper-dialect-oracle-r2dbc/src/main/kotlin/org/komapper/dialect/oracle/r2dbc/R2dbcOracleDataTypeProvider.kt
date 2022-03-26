package org.komapper.dialect.oracle.r2dbc

import org.komapper.r2dbc.AbstractR2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcBigDecimalType
import org.komapper.r2dbc.R2dbcBigIntegerType
import org.komapper.r2dbc.R2dbcBlobType
import org.komapper.r2dbc.R2dbcByteArrayType
import org.komapper.r2dbc.R2dbcByteType
import org.komapper.r2dbc.R2dbcClobType
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDoubleType
import org.komapper.r2dbc.R2dbcFloatType
import org.komapper.r2dbc.R2dbcInstantType
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

class R2dbcOracleDataTypeProvider(next: R2dbcDataTypeProvider) :
    AbstractR2dbcDataTypeProvider(next, DEFAULT_DATA_TYPES) {

    companion object {
        val DEFAULT_DATA_TYPES: List<R2dbcDataType<*>> = listOf(
            R2dbcBigDecimalType("decimal"),
            R2dbcBigIntegerType("decimal"),
            R2dbcBlobType("blob"),
            R2dbcByteType("integer"),
            R2dbcByteArrayType("raw"),
            R2dbcClobType("clob"),
            R2dbcDoubleType("float"),
            R2dbcFloatType("float"),
            R2dbcInstantType("timestamp"),
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
            R2dbcOracleBooleanType,
            R2dbcOracleDurationType,
            R2dbcOraclePeriodType
        )
    }
}
