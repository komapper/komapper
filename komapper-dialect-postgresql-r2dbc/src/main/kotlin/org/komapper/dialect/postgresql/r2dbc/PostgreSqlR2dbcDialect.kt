package org.komapper.dialect.postgresql.r2dbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.r2dbc.AbstractR2dbcDialect
import org.komapper.r2dbc.ArrayType
import org.komapper.r2dbc.BigDecimalType
import org.komapper.r2dbc.BigIntegerType
import org.komapper.r2dbc.Binder
import org.komapper.r2dbc.BooleanType
import org.komapper.r2dbc.ByteArrayType
import org.komapper.r2dbc.ByteType
import org.komapper.r2dbc.DoubleType
import org.komapper.r2dbc.FloatType
import org.komapper.r2dbc.IndexedBinder
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

open class PostgreSqlR2dbcDialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList(),
    val version: Version = Version.V0_9
) : PostgreSqlDialect, AbstractR2dbcDialect(defaultDataTypes + dataTypes) {

    companion object {
        enum class Version { V0_9 }

        val defaultDataTypes: List<R2dbcDataType<*>> = listOf(
            ArrayType("array"),
            BigDecimalType("decimal"),
            BigIntegerType("decimal"),
            BooleanType("boolean"),
            ByteType("smallint"),
            ByteArrayType("bytea"),
            DoubleType("double precision"),
            FloatType("real"),
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

    override fun getBinder(): Binder {
        return IndexedBinder
    }
}
