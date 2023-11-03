package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Path
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType

object PostgreSqlR2dbcPathType : AbstractR2dbcDataType<Path>(Path::class) {
    override val name: String = "path"

    override fun getValue(row: Row, index: Int): Path? {
        return row.get(index, Path::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Path? {
        return row.get(columnLabel, Path::class.java)
    }
}
