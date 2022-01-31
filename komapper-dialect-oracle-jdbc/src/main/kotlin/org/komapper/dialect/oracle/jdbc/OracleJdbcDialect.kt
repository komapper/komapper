package org.komapper.dialect.oracle.jdbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.jdbc.AbstractJdbcDialect
import org.komapper.jdbc.BigDecimalType
import org.komapper.jdbc.BigIntegerType
import org.komapper.jdbc.BlobType
import org.komapper.jdbc.ByteArrayType
import org.komapper.jdbc.ByteType
import org.komapper.jdbc.ClobType
import org.komapper.jdbc.DoubleType
import org.komapper.jdbc.FloatType
import org.komapper.jdbc.IntType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.LocalDateTimeType
import org.komapper.jdbc.LocalDateType
import org.komapper.jdbc.LocalTimeType
import org.komapper.jdbc.LongType
import org.komapper.jdbc.OffsetDateTimeType
import org.komapper.jdbc.ShortType
import org.komapper.jdbc.StringType
import org.komapper.jdbc.UByteType
import org.komapper.jdbc.UIntType
import org.komapper.jdbc.UShortType
import java.sql.SQLException

class OracleJdbcDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : OracleDialect, AbstractJdbcDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 1

        // TODO
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
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

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().all {
            return it.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }

    override fun supportsReturnGeneratedKeysFlag(): Boolean = false
}
