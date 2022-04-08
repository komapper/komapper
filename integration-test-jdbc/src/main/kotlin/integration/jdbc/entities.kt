package integration.jdbc

import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import java.sql.Array
import java.sql.Blob
import java.sql.Clob
import java.sql.SQLXML

@KomapperEntity
@KomapperTable("array_test")
data class ArrayTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Array?)

@KomapperEntity
@KomapperTable("blob_test")
data class BlobTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Blob?)

@KomapperEntity
@KomapperTable("clob_test")
data class ClobTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Clob?)

@KomapperEntity
@KomapperTable("sqlxml_test")
data class SqlXmlTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: SQLXML?)
