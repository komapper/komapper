package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Lseg
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType

object PostgreSqlR2dbcLsegType : AbstractR2dbcDataType<Lseg>(Lseg::class) {
    override val name: String = "lseg"

    override fun getValue(row: Row, index: Int): Lseg? {
        return row.get(index, Lseg::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Lseg? {
        return row.get(columnLabel, Lseg::class.java)
    }
}
