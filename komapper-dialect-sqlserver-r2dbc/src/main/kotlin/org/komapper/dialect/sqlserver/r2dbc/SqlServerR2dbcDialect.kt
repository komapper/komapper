package org.komapper.dialect.sqlserver.r2dbc

import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.r2dbc.AbstractR2dbcDialect
import org.komapper.r2dbc.ArrayType
import org.komapper.r2dbc.AtSignBinder
import org.komapper.r2dbc.BigDecimalType
import org.komapper.r2dbc.BigIntegerType
import org.komapper.r2dbc.Binder
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
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.ShortType
import org.komapper.r2dbc.StringType
import org.komapper.r2dbc.UByteType
import org.komapper.r2dbc.UIntType
import org.komapper.r2dbc.UShortType

class SqlServerR2dbcDialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : SqlServerDialect, AbstractR2dbcDialect(defaultDataTypes + dataTypes) {

    companion object {
        val defaultDataTypes: List<R2dbcDataType<*>> = listOf(
            ArrayType("array"),
            BigDecimalType("decimal"),
            BigIntegerType("decimal"),
            BooleanType("bit"),
            ByteType("smallint"),
            ByteArrayType("varbinary(1000)"),
            DoubleType("real"),
            FloatType("float"),
            IntType("int"),
            LocalDateTimeType("datetime"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            ShortType("smallint"),
            StringType("varchar(1000)"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("int"),
            SqlServerBitType,
        )
    }

    enum class Version { IMPLICIT }

    override fun getBinder(): Binder {
        return AtSignBinder
    }
}
