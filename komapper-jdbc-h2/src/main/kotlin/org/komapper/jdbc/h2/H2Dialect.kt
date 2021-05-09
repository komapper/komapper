package org.komapper.jdbc.h2

import org.komapper.core.AbstractDialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.jdbc.AnyType
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

open class H2Dialect(dataTypes: List<DataType<*>> = emptyList(), val version: Version = Version.V1_4) :
    AbstractDialect(defaultDataTypes + dataTypes) {

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

    override fun getDefaultSchemaName(userName: String?): String? {
        return null
    }
}
