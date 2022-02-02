package org.komapper.dialect.h2.r2dbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcAnyType
import org.komapper.r2dbc.R2dbcArrayType
import org.komapper.r2dbc.R2dbcBigDecimalType
import org.komapper.r2dbc.R2dbcBigIntegerType
import org.komapper.r2dbc.R2dbcBlobType
import org.komapper.r2dbc.R2dbcBooleanType
import org.komapper.r2dbc.R2dbcByteArrayType
import org.komapper.r2dbc.R2dbcByteType
import org.komapper.r2dbc.R2dbcClobType
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.R2dbcDoubleType
import org.komapper.r2dbc.R2dbcFloatType
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

open class R2dbcH2Dialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : H2Dialect, R2dbcAbstractDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        const val DRIVER = "h2"

        val DEFAULT_DATA_TYPES: List<R2dbcDataType<*>> = listOf(
            R2dbcAnyType("other"),
            R2dbcArrayType("array"),
            R2dbcBigDecimalType("bigint"),
            R2dbcBigIntegerType("bigint"),
            R2dbcBlobType("blob"),
            R2dbcBooleanType("bool"),
            R2dbcByteType("tinyint"),
            R2dbcByteArrayType("binary"),
            R2dbcClobType("clob"),
            R2dbcDoubleType("double"),
            R2dbcFloatType("float"),
            R2dbcIntType("integer"),
            R2dbcLocalDateTimeType("timestamp"),
            R2dbcLocalDateType("date"),
            R2dbcLocalTimeType("time"),
            R2dbcLongType("bigint"),
            R2dbcOffsetDateTimeType("timestamp with time zone"),
            R2dbcShortType("smallint"),
            R2dbcStringType("varchar(500)"),
            R2dbcUByteType("smallint"),
            R2dbcUIntType("bigint"),
            R2dbcUShortType("integer"),
            R2dbcH2UUIDType
        )
    }

    enum class Version { IMPLICIT }

    override val driver: String = DRIVER
}
