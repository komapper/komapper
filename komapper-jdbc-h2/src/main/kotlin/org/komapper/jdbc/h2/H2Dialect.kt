package org.komapper.jdbc.h2

import org.komapper.core.config.AbstractDialect
import java.sql.SQLException

open class H2Dialect(val version: Version = Version.V1_4) : AbstractDialect() {

    companion object {
        enum class Version { V1_4 }

        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505
    }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "call next value for $sequenceName"
    }

    override fun supportsMerge(): Boolean = true

    override fun supportsUpsert(): Boolean = false
}
