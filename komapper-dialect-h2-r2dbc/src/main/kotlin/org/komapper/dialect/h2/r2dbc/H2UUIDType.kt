package org.komapper.dialect.h2.r2dbc

import org.komapper.r2dbc.AbstractDataType
import java.util.UUID

object H2UUIDType : AbstractDataType<UUID>(UUID::class) {
    override val name: String = "uuid"

    override fun convert(value: Any): UUID {
        return when (value) {
            is UUID -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}
