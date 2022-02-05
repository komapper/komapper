package integration.jdbc

import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import java.sql.Blob
import java.sql.Clob

@KomapperEntity
@KomapperTable("blob_test")
data class BlobTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true)val value: Blob)

@KomapperEntity
@KomapperTable("clob_test")
data class ClobTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true)val value: Clob)
