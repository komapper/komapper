package org.komapper.core.query

import org.komapper.core.jdbc.AbstractDialect
import java.sql.SQLException

class EmptyDialect : AbstractDialect() {
    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }
}
