package org.komapper.jdbc.dialect.postgresql

import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.jdbc.AbstractJdbcDialect
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

open class PostgreSqlJdbcDialect(dataTypes: List<DataType<*>> = emptyList(), val version: Version = Version.V42_2) :
    AbstractJdbcDialect(defaultDataTypes + dataTypes) {

    companion object {
        enum class Version { V42_2 }

        const val subprotocol = "postgresql"

        /** the state code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE = "23505"

        val defaultDataTypes: List<DataType<*>> = listOf(
            ArrayType("array"),
            BigDecimalType("decimal"),
            BigIntegerType("decimal"),
            BlobType("blob"),
            BooleanType("boolean"),
            ByteType("smallint"),
            ByteArrayType("bytea"),
            DoubleType("double precision"),
            ClobType("text"),
            FloatType("real"),
            IntType("integer"),
            LocalDateTimeType("timestamp"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            NClobType("text"),
            OffsetDateTimeType("timestamp with time zone"),
            ShortType("smallint"),
            StringType("varchar(500)"),
            SQLXMLType("text"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("integer"),
            PostgreSqlUUIDType
        )
    }

    override val subprotocol: String = Companion.subprotocol

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.sqlState == UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "select nextval('$sequenceName')"
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return PostgreSqlSchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY> {
        return PostgreSqlEntityUpsertStatementBuilder(this, context, entities)
    }
}
