package org.komapper.jdbc.h2

import org.komapper.core.AbstractDialect
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import java.sql.SQLException

open class H2Dialect(val version: Version = Version.V1_4) : AbstractDialect() {

    companion object {
        enum class Version { V1_4 }

        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505
    }

    override val schemaStatementBuilder: SchemaStatementBuilder by lazy {
        H2SchemaStatementBuilder(this)
    }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "call next value for $sequenceName"
    }
}
