package org.komapper.dialect.postgresql.jdbc

import org.komapper.core.type.BlobByteArray
import org.komapper.jdbc.AbstractJdbcDataType
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.typeOf

class PostgreSqlJdbcBlobByteArrayType :
    AbstractJdbcDataType<BlobByteArray>(typeOf<BlobByteArray>(), JDBCType.BINARY) {
    override val name: String = "bytea"
    override fun doGetValue(rs: ResultSet, index: Int): BlobByteArray? {
        return rs.getBytes(index)?.let { BlobByteArray(it) }
    }
    override fun doGetValue(rs: ResultSet, columnLabel: String): BlobByteArray? {
        return rs.getBytes(columnLabel)?.let { BlobByteArray(it) }
    }
    override fun doSetValue(ps: PreparedStatement, index: Int, value: BlobByteArray) {
        ps.setBytes(index, value.value)
    }
}
