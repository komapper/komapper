package org.komapper.dialect.oracle.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.oracle.OracleDialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcBigDecimalType
import org.komapper.r2dbc.R2dbcBigIntegerType
import org.komapper.r2dbc.R2dbcBlobType
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

open class R2dbcOracleDialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList()
) : OracleDialect, R2dbcAbstractDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        val DEFAULT_DATA_TYPES: List<R2dbcDataType<*>> = listOf(
            R2dbcBigDecimalType("decimal"),
            R2dbcBigIntegerType("decimal"),
            R2dbcBlobType("blob"),
            R2dbcByteType("integer"),
            R2dbcByteArrayType("raw"),
            R2dbcClobType("clob"),
            R2dbcDoubleType("float"),
            R2dbcFloatType("float"),
            R2dbcIntType("integer"),
            R2dbcLocalDateTimeType("timestamp"),
            R2dbcLocalDateType("date"),
            R2dbcLocalTimeType("date"),
            R2dbcLongType("integer"),
            R2dbcOffsetDateTimeType("timestamp with time zone"),
            R2dbcShortType("integer"),
            R2dbcStringType("varchar2(1000)"),
            R2dbcUByteType("integer"),
            R2dbcUIntType("integer"),
            R2dbcUShortType("integer"),
            R2dbcOracleBooleanType,
            R2dbcOracleDurationType,
            R2dbcOraclePeriodType
        )
    }

    override fun isSequenceExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.NAME_ALREADY_USED_ERROR_CODE
    }

    override fun isSequenceNotExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.SEQUENCE_NOT_EXISTS_ERROR_CODE
    }

    override fun isTableExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.NAME_ALREADY_USED_ERROR_CODE
    }

    override fun isTableNotExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.TABLE_NOT_EXISTS_ERROR_CODE
    }

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }
}
