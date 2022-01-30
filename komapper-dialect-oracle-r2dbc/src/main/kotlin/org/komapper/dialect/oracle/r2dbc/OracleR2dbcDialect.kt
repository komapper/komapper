package org.komapper.dialect.oracle.r2dbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.r2dbc.AbstractR2dbcDialect
import org.komapper.r2dbc.BigDecimalType
import org.komapper.r2dbc.BigIntegerType
import org.komapper.r2dbc.BlobType
import org.komapper.r2dbc.ByteArrayType
import org.komapper.r2dbc.ByteType
import org.komapper.r2dbc.ClobType
import org.komapper.r2dbc.DoubleType
import org.komapper.r2dbc.FloatType
import org.komapper.r2dbc.IntType
import org.komapper.r2dbc.LocalDateTimeType
import org.komapper.r2dbc.LocalDateType
import org.komapper.r2dbc.LocalTimeType
import org.komapper.r2dbc.LongType
import org.komapper.r2dbc.OffsetDateTimeType
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.ShortType
import org.komapper.r2dbc.StringType
import org.komapper.r2dbc.UByteType
import org.komapper.r2dbc.UIntType
import org.komapper.r2dbc.UShortType

open class OracleR2dbcDialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : OracleDialect, AbstractR2dbcDialect(defaultDataTypes + dataTypes) {

    companion object {
        val defaultDataTypes: List<R2dbcDataType<*>> = listOf(
            BigDecimalType("decimal"),
            BigIntegerType("decimal"),
            BlobType("blob"),
            ByteType("integer"),
            ByteArrayType("raw"),
            ClobType("clob"),
            DoubleType("float"),
            FloatType("float"),
            IntType("integer"),
            LocalDateTimeType("timestamp"),
            LocalDateType("date"),
            LocalTimeType("date"),
            LongType("integer"),
            OffsetDateTimeType("timestamp with time zone"),
            ShortType("integer"),
            StringType("varchar2(1000)"),
            UByteType("integer"),
            UIntType("integer"),
            UShortType("integer"),
            OracleBooleanType
        )
    }

    enum class Version { IMPLICIT }
}
