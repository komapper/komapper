package org.komapper.dialect.h2.jdbc

import org.komapper.jdbc.AbstractJdbcDataTypeProvider
import org.komapper.jdbc.JdbcAnyType
import org.komapper.jdbc.JdbcArrayType
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
import org.komapper.jdbc.JdbcInstantType
import org.komapper.jdbc.JdbcIntType
import org.komapper.jdbc.JdbcKotlinInstantType
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

class H2JdbcDataTypeProvider(next: JdbcDataTypeProvider) :
    AbstractJdbcDataTypeProvider(next, DEFAULT_DATA_TYPES) {
    companion object {
        private val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcAnyType("java_object"),
            JdbcArrayType("varchar(100) array"),
            JdbcBigDecimalType("numeric"),
            JdbcBigIntegerType("bigint"),
            JdbcBlobType("blob"),
            JdbcBooleanType("bool"),
            JdbcByteType("tinyint"),
            JdbcByteArrayType("binary"),
            JdbcBlobByteArrayType("blob"),
            JdbcDoubleType("double"),
            JdbcClobType("clob"),
            JdbcClobStringType("clob"),
            JdbcFloatType("float"),
            JdbcInstantType("timestamp with time zone"),
            JdbcIntType("integer"),
            JdbcKotlinInstantType("timestamp with time zone"),
            JdbcLocalDateTimeType("timestamp"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("time"),
            JdbcLongType("bigint"),
            JdbcNClobType("nclob"),
            JdbcOffsetDateTimeType("timestamp with time zone"),
            JdbcShortType("smallint"),
            JdbcStringType("varchar(500)"),
            JdbcSQLXMLType("clob"),
            JdbcUByteType("smallint"),
            JdbcUIntType("bigint"),
            JdbcUShortType("integer"),
            H2JdbcUUIDType,
        )
    }
}
