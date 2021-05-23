package org.komapper.jdbc.dialect.h2

import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.jdbc.AbstractJdbcDialect
import org.komapper.jdbc.AnyType
import org.komapper.jdbc.ArrayType
import org.komapper.jdbc.BigDecimalType
import org.komapper.jdbc.BigIntegerType
import org.komapper.jdbc.BlobType
import org.komapper.jdbc.BooleanType
import org.komapper.jdbc.ByteArrayType
import org.komapper.jdbc.ByteType
import org.komapper.jdbc.ClobType
import org.komapper.jdbc.DataType
import org.komapper.jdbc.DoubleType
import org.komapper.jdbc.FloatType
import org.komapper.jdbc.IntType
import org.komapper.jdbc.LocalDateTimeType
import org.komapper.jdbc.LocalDateType
import org.komapper.jdbc.LocalTimeType
import org.komapper.jdbc.LongType
import org.komapper.jdbc.NClobType
import org.komapper.jdbc.OffsetDateTimeType
import org.komapper.jdbc.SQLXMLType
import org.komapper.jdbc.ShortType
import org.komapper.jdbc.StringType
import org.komapper.jdbc.UByteType
import org.komapper.jdbc.UIntType
import org.komapper.jdbc.UShortType
import java.sql.SQLException

open class H2JdbcDialect(dataTypes: List<DataType<*>> = emptyList(), val version: Version = Version.V1_4) :
    AbstractJdbcDialect(defaultDataTypes + dataTypes) {

    companion object {
        enum class Version { V1_4 }

        const val subprotocol = "h2"

        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505

        val defaultDataTypes: List<DataType<*>> = listOf(
            AnyType("other"),
            ArrayType("array"),
            BigDecimalType("bigint"),
            BigIntegerType("bigint"),
            BlobType("blob"),
            BooleanType("bool"),
            ByteType("tinyint"),
            ByteArrayType("binary"),
            DoubleType("double"),
            ClobType("clob"),
            FloatType("float"),
            IntType("integer"),
            LocalDateTimeType("timestamp"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            NClobType("nclob"),
            OffsetDateTimeType("timestamp with time zone"),
            ShortType("smallint"),
            StringType("varchar(500)"),
            SQLXMLType("clob"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("integer"),
            H2UUIDType
        )
    }

    override val subprotocol: String = Companion.subprotocol

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "call next value for $sequenceName"
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return H2SchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return H2EntityUpsertStatementBuilder(this, context, entities)
    }
}
