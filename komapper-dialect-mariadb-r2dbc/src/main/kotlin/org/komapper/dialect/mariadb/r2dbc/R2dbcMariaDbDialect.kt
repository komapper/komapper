package org.komapper.dialect.mariadb.r2dbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcArrayType
import org.komapper.r2dbc.R2dbcBigDecimalType
import org.komapper.r2dbc.R2dbcBigIntegerType
import org.komapper.r2dbc.R2dbcBooleanType
import org.komapper.r2dbc.R2dbcByteArrayType
import org.komapper.r2dbc.R2dbcByteType
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

open class R2dbcMariaDbDialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : MariaDbDialect, R2dbcAbstractDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        val DEFAULT_DATA_TYPES: List<R2dbcDataType<*>> = listOf(
            R2dbcArrayType("varbinary(500)"),
            R2dbcBigDecimalType("decimal"),
            R2dbcBigIntegerType("decimal"),
            R2dbcBooleanType("bit(1)"),
            R2dbcByteType("tinyint"),
            R2dbcByteArrayType("bytea"),
            R2dbcDoubleType("double precision"),
            R2dbcFloatType("real"),
            R2dbcIntType("integer"),
            R2dbcLocalDateTimeType("timestamp(6)"),
            R2dbcLocalDateType("date"),
            R2dbcLocalTimeType("time"),
            R2dbcLongType("bigint"),
            R2dbcOffsetDateTimeType("timestamp"),
            R2dbcShortType("smallint"),
            R2dbcStringType("varchar(500)"),
            R2dbcUByteType("smallint"),
            R2dbcUIntType("bigint"),
            R2dbcUShortType("integer"),
        )
    }

    enum class Version { IMPLICIT }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}
