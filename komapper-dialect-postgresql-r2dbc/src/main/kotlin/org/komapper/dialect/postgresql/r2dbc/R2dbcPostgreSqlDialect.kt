package org.komapper.dialect.postgresql.r2dbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.r2dbc.Binder
import org.komapper.r2dbc.IndexedBinder
import org.komapper.r2dbc.R2dbcAbstractDialect
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

open class R2dbcPostgreSqlDialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : PostgreSqlDialect, R2dbcAbstractDialect(defaultDataTypes + dataTypes) {

    companion object {
        val defaultDataTypes: List<R2dbcDataType<*>> = listOf(
            R2dbcArrayType("array"),
            R2dbcBigDecimalType("decimal"),
            R2dbcBigIntegerType("decimal"),
            R2dbcBlobType("bytea"),
            R2dbcBooleanType("boolean"),
            R2dbcByteType("smallint"),
            R2dbcByteArrayType("bytea"),
            R2dbcClobType("text"),
            R2dbcDoubleType("double precision"),
            R2dbcFloatType("real"),
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
            R2dbcPostgresqlJsonType,
            R2dbcPostgreSqlUUIDType
        )
    }

    enum class Version { IMPLICIT }

    override fun getBinder(): Binder {
        return IndexedBinder
    }
}
