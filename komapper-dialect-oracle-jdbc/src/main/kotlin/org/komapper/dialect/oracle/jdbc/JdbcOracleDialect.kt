package org.komapper.dialect.oracle.jdbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcBigDecimalType
import org.komapper.jdbc.JdbcBigIntegerType
import org.komapper.jdbc.JdbcBlobType
import org.komapper.jdbc.JdbcByteArrayType
import org.komapper.jdbc.JdbcByteType
import org.komapper.jdbc.JdbcClobType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDoubleType
import org.komapper.jdbc.JdbcFloatType
import org.komapper.jdbc.JdbcIntType
import org.komapper.jdbc.JdbcLocalDateTimeType
import org.komapper.jdbc.JdbcLocalDateType
import org.komapper.jdbc.JdbcLocalTimeType
import org.komapper.jdbc.JdbcLongType
import org.komapper.jdbc.JdbcOffsetDateTimeType
import org.komapper.jdbc.JdbcShortType
import org.komapper.jdbc.JdbcStringType
import org.komapper.jdbc.JdbcUByteType
import org.komapper.jdbc.JdbcUIntType
import org.komapper.jdbc.JdbcUShortType
import java.sql.SQLException

class JdbcOracleDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : OracleDialect, JdbcAbstractDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 1

        // TODO
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcBigDecimalType("decimal"),
            JdbcBigIntegerType("decimal"),
            JdbcBlobType("blob"),
            JdbcByteType("integer"),
            JdbcByteArrayType("raw"),
            JdbcClobType("clob"),
            JdbcDoubleType("float"),
            JdbcFloatType("float"),
            JdbcIntType("integer"),
            JdbcLocalDateTimeType("timestamp"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("date"),
            JdbcLongType("integer"),
            JdbcOffsetDateTimeType("timestamp with time zone"),
            JdbcShortType("integer"),
            JdbcStringType("varchar2(1000)"),
            JdbcUByteType("integer"),
            JdbcUIntType("integer"),
            JdbcUShortType("integer"),
            JdbcOracleBooleanType
        )
    }

    enum class Version { IMPLICIT }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().all {
            return it.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }

    override fun supportsReturnGeneratedKeysFlag(): Boolean = false
}