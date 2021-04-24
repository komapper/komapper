package integration

import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.annotation.KmTable
import org.komapper.annotation.KmVersion

@KmEntity
@KmTable(catalog = "catalog", schema = "schema", alwaysQuote = true)
data class CatalogAndSchema(
    @KmId @KmColumn(name = "ADDRESS_ID") val addressId: Int,
    val street: String,
    @KmVersion val version: Int
) {
    companion object
}

@KmEntity
@KmTable(catalog = "catalog", alwaysQuote = true)
data class CatalogOnly(
    @KmId val id: Int,
) {
    companion object
}

@KmEntity
@KmTable(schema = "schema", alwaysQuote = true)
data class SchemaOnly(
    @KmId val id: Int,
) {
    companion object
}

@KmEntity
@KmTable("    ", alwaysQuote = true)
data class BlankName(
    @KmId @KmColumn("    ", alwaysQuote = true) val id: Int,
) {
    companion object
}

@KmEntity
@KmTable(alwaysQuote = true)
data class Order(
    @KmId @KmColumn(alwaysQuote = true) val orderId: Int,
    @KmColumn(alwaysQuote = true) val value: String
) {
    companion object
}
