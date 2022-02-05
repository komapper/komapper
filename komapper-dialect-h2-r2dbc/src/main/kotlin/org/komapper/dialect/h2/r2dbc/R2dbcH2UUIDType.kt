package org.komapper.dialect.h2.r2dbc

import org.komapper.r2dbc.R2dbcAbstractType
import java.util.UUID

object R2dbcH2UUIDType : R2dbcAbstractType<UUID>(UUID::class) {
    override val name: String = "uuid"

    override fun convert(value: Any): UUID {
        return when (value) {
            is UUID -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}
