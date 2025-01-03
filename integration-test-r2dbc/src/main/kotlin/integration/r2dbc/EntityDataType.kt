package integration.r2dbc

import io.r2dbc.spi.Blob
import io.r2dbc.spi.Clob
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.core.type.ClobString

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_data")
data class ArrayData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<String>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_data")
data class ArrayOfNullableData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<String?>?,
)

@KomapperEntity
@KomapperTable("blob_data")
data class BlobData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Blob?,
)

@KomapperEntity
@KomapperTable("clob_data")
data class ClobData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Clob?,
)

@KomapperEntity
@KomapperTable("clob_data")
data class ClobStringData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true, alternateType = ClobString::class)
    val value: String?,
)
