package org.komapper.dialect.oracle.jdbc

import org.komapper.jdbc.JdbcAbstractDataTypeProvider
import org.komapper.jdbc.JdbcBigDecimalType
import org.komapper.jdbc.JdbcBigIntegerType
import org.komapper.jdbc.JdbcBlobType
import org.komapper.jdbc.JdbcByteArrayType
import org.komapper.jdbc.JdbcByteType
import org.komapper.jdbc.JdbcClobType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDoubleType
import org.komapper.jdbc.JdbcFloatType
import org.komapper.jdbc.JdbcInstantType
import org.komapper.jdbc.JdbcIntType
import org.komapper.jdbc.JdbcLocalDateTimeType
import org.komapper.jdbc.JdbcLocalDateType
import org.komapper.jdbc.JdbcLocalTimeType
import org.komapper.jdbc.JdbcLongType
import org.komapper.jdbc.JdbcOffsetDateTimeType
import org.komapper.jdbc.JdbcShortType
import org.komapper.jdbc.JdbcStringType
import org.komapper.jdbc.JdbcUByteType
import org.komapper.jdbc.JdbcUIntType
import org.komapper.jdbc.JdbcUShortType

class JdbcOracleDataTypeProvider(next: JdbcDataTypeProvider) : JdbcAbstractDataTypeProvider(next, DEFAULT_DATA_TYPES) {

    companion object {
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcBigDecimalType("decimal"),
            JdbcBigIntegerType("decimal"),
            JdbcBlobType("blob"),
            JdbcByteType("integer"),
            JdbcByteArrayType("raw"),
            JdbcClobType("clob"),
            JdbcDoubleType("float"),
            JdbcFloatType("float"),
            JdbcInstantType("timestamp with time zone"),
            JdbcIntType("integer"),
            JdbcLocalDateTimeType("timestamp"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("date"),
            JdbcLongType("integer"),
            JdbcOffsetDateTimeType("timestamp with time zone"),
            JdbcShortType("integer"),
            JdbcStringType("varchar2(1000)"),
            JdbcUByteType("integer"),
            JdbcUIntType("integer"),
            JdbcUShortType("integer"),
            JdbcOracleBooleanType
        )
    }
}
