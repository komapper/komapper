package org.komapper.dialect.postgresql.r2dbc

import org.komapper.r2dbc.AbstractR2dbcDataType
import java.util.UUID

object R2dbcPostgreSqlUUIDType : AbstractR2dbcDataType<UUID>(UUID::class) {
    override val name: String = "uuid"

    override fun convertBeforeGetting(value: Any): UUID {
        return when (value) {
            is UUID -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}
