package org.komapper.jdbc.dialect.postgresql

import org.komapper.jdbc.AbstractDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

object PostgreSqlUUIDType : AbstractDataType<UUID>(UUID::class, JDBCType.OTHER) {
    override val name: String = "uuid"

    override fun doGetValue(rs: ResultSet, index: Int): UUID? {
        val value = rs.getString(index)
        return toUUID(value)
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): UUID? {
        val value = rs.getString(columnLabel)
        return toUUID(value)
    }

    private fun toUUID(value: String?): UUID? {
        return value?.let { UUID.fromString(value) }
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: UUID) {
        ps.setObject(index, value, jdbcType.vendorTypeNumber)
    }
}
