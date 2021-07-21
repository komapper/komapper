package org.komapper.dialect.mysql.r2dbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.r2dbc.AbstractR2dbcDialect
import org.komapper.r2dbc.ArrayType
import org.komapper.r2dbc.BigDecimalType
import org.komapper.r2dbc.BigIntegerType
import org.komapper.r2dbc.BooleanType
import org.komapper.r2dbc.ByteArrayType
import org.komapper.r2dbc.ByteType
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

open class MySqlR2dbcDialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : MySqlDialect, AbstractR2dbcDialect(defaultDataTypes + dataTypes) {

    companion object {
        val defaultDataTypes: List<R2dbcDataType<*>> = listOf(
            ArrayType("varbinary(500)"),
            BigDecimalType("decimal"),
            BigIntegerType("decimal"),
            BooleanType("bit(1)"),
            ByteType("tinyint"),
            ByteArrayType("bytea"),
            DoubleType("double precision"),
            FloatType("real"),
            IntType("integer"),
            LocalDateTimeType("timestamp(6)"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            OffsetDateTimeType("timestamp"),
            ShortType("smallint"),
            StringType("varchar(500)"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("integer"),
        )
    }

    enum class Version { IMPLICIT }
}
