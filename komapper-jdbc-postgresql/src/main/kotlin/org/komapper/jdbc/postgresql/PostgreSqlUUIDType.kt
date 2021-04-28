package org.komapper.jdbc.postgresql

import org.komapper.core.jdbc.AbstractDataType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID

object PostgreSqlUUIDType : AbstractDataType<UUID>(UUID::class, Types.OTHER) {
    override val name: String = "uuid"

    override fun doGetValue(rs: ResultSet, index: Int): UUID? {
        val value = rs.getString(index)
        return value?.let { UUID.fromString(it) }
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): UUID? {
        val value = rs.getString(columnLabel)
        return value?.let { UUID.fromString(it) }
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: UUID) {
        ps.setObject(index, value, sqlType)
    }
}
