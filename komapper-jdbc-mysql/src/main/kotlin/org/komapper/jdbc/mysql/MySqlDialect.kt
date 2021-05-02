package org.komapper.jdbc.mysql

import org.komapper.core.AbstractDialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.jdbc.ArrayType
import org.komapper.core.jdbc.BigDecimalType
import org.komapper.core.jdbc.BigIntegerType
import org.komapper.core.jdbc.BlobType
import org.komapper.core.jdbc.BooleanType
import org.komapper.core.jdbc.ByteArrayType
import org.komapper.core.jdbc.ByteType
import org.komapper.core.jdbc.ClobType
import org.komapper.core.jdbc.DataType
import org.komapper.core.jdbc.DoubleType
import org.komapper.core.jdbc.FloatType
import org.komapper.core.jdbc.IntType
import org.komapper.core.jdbc.LocalDateTimeType
import org.komapper.core.jdbc.LocalDateType
import org.komapper.core.jdbc.LocalTimeType
import org.komapper.core.jdbc.LongType
import org.komapper.core.jdbc.NClobType
import org.komapper.core.jdbc.OffsetDateTimeType
import org.komapper.core.jdbc.SQLXMLType
import org.komapper.core.jdbc.ShortType
import org.komapper.core.jdbc.StringType
import org.komapper.core.jdbc.UByteType
import org.komapper.core.jdbc.UIntType
import org.komapper.core.jdbc.UShortType
import java.sql.SQLException

open class MySqlDialect(dataTypes: Set<DataType<*>> = emptySet(), val version: Version = Version.V8_0) :
    AbstractDialect(
        defaultDataTypes + dataTypes
    ) {

    companion object {
        enum class Version { V8_0 }

        const val subprotocol = "mysql"

        /** the error code that represents unique violation  */
        var UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES = setOf(1022, 1062)

        val defaultDataTypes: Set<DataType<*>> = setOf(
            ArrayType("varbinary(500)"),
            BigDecimalType("decimal"),
            BigIntegerType("decimal"),
            BlobType("blob"),
            BooleanType("bit(1)"),
            ByteType("tinyint"),
            ByteArrayType("bytea"),
            DoubleType("double precision"),
            ClobType("text"),
            FloatType("real"),
            IntType("integer"),
            LocalDateTimeType("timestamp(6)"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            NClobType("text"),
            OffsetDateTimeType("timestamp"),
            ShortType("smallint"),
            StringType("varchar(500)"),
            SQLXMLType("text"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("integer"),
        )
    }

    override val subprotocol: String = Companion.subprotocol
    override val openQuote: String = "`"
    override val closeQuote: String = "`"

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.errorCode in UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder {
        return MySqlOffsetLimitStatementBuilder(this, offset, limit)
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return MySqlSchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return MySqlEntityUpsertStatementBuilder(this, context, entities)
    }
}
