package org.komapper.dialect.h2.jdbc

import org.komapper.jdbc.JdbcAbstractType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

object JdbcH2UUIDType : JdbcAbstractType<UUID>(UUID::class, JDBCType.OTHER) {
    override val name: String = "uuid"

    override fun doGetValue(rs: ResultSet, index: Int): UUID? {
        return rs.getObject(index) as? UUID
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): UUID? {
        return rs.getObject(columnLabel) as? UUID
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: UUID) {
        ps.setObject(index, value)
    }
}
