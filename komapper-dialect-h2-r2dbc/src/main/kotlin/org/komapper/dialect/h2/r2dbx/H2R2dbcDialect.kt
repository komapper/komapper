package org.komapper.dialect.h2.r2dbx

import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.AbstractR2dbcDialect
import org.komapper.r2dbc.AnyType
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

open class H2R2dbcDialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : H2Dialect, AbstractR2dbcDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        const val DRIVER = "h2"

        val DEFAULT_DATA_TYPES: List<R2dbcDataType<*>> = listOf(
            AnyType("other"),
            ArrayType("array"),
            BigDecimalType("bigint"),
            BigIntegerType("bigint"),
            BooleanType("bool"),
            ByteType("tinyint"),
            ByteArrayType("binary"),
            DoubleType("double"),
            FloatType("float"),
            IntType("integer"),
            LocalDateTimeType("timestamp"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            OffsetDateTimeType("timestamp with time zone"),
            ShortType("smallint"),
            StringType("varchar(500)"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("integer"),
        )
    }

    enum class Version { IMPLICIT }

    override val driver: String = DRIVER
}
