package org.komapper.dialect.sqlserver.jdbc

import org.komapper.dialect.sqlserver.SqlServerDialect
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
import org.komapper.jdbc.SQLXMLType
import org.komapper.jdbc.ShortType
import org.komapper.jdbc.StringType
import org.komapper.jdbc.UByteType
import org.komapper.jdbc.UIntType
import org.komapper.jdbc.UShortType
import java.sql.SQLException

class SqlServerJdbcDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : SqlServerDialect, AbstractJdbcDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 2627

        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            BigDecimalType("decimal"),
            BigIntegerType("decimal"),
            BlobType("varbinary(max)"),
            ByteType("smallint"),
            ByteArrayType("varbinary(1000)"),
            ClobType("text"),
            DoubleType("real"),
            FloatType("float"),
            IntType("int"),
            LocalDateTimeType("datetime"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            ShortType("smallint"),
            SQLXMLType("xml"),
            StringType("varchar(1000)"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("int"),
            SqlServerBitType,
        )
    }

    enum class Version { IMPLICIT }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().all {
            return it.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }
}
