package integration.r2dbc

import io.r2dbc.spi.Blob
import io.r2dbc.spi.Clob
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_test")
data class ArrayTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Array<String>?)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_test")
data class ArrayOfNullableTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Array<String?>?)

@KomapperEntity
@KomapperTable("blob_test")
data class BlobTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true)val value: Blob?)

@KomapperEntity
@KomapperTable("clob_test")
data class ClobTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true)val value: Clob?)
