package org.komapper.dialect.postgresql.r2dbc

import org.komapper.r2dbc.AbstractR2dbcDataType
import java.util.UUID
import kotlin.reflect.typeOf

object PostgreSqlR2dbcUUIDType : AbstractR2dbcDataType<UUID>(typeOf<UUID>()) {
    override val name: String = "uuid"

    override fun convertBeforeGetting(value: Any): UUID {
        return when (value) {
            is UUID -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}
