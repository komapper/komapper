package integration.jdbc

import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.core.type.ClobString
import java.sql.Array
import java.sql.Blob
import java.sql.Clob
import java.sql.SQLXML

@KomapperEntity
@KomapperTable("array_data")
data class ArrayData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array?
)

@KomapperEntity
@KomapperTable("blob_data")
data class BlobData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Blob?
)

@KomapperEntity
@KomapperTable("clob_data")
data class ClobData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Clob?
)

@KomapperEntity
@KomapperTable("clob_data")
data class ClobStringData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true, alternateType = ClobString::class)
    val value: String?,
)

@KomapperEntity
@KomapperTable("sqlxml_data")
data class SqlXmlData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: SQLXML?
)
