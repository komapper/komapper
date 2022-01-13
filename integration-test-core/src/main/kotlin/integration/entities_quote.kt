package integration

import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion

@KomapperEntity
@KomapperTable(catalog = "catalog", schema = "schema", alwaysQuote = true)
data class CatalogAndSchema(
    @KomapperId @KomapperColumn(name = "address_id") val addressId: Int,
    val street: String,
    @KomapperVersion val version: Int
)

@KomapperEntity
@KomapperTable(catalog = "catalog", alwaysQuote = true)
data class CatalogOnly(
    @KomapperId val id: Int,
)

@KomapperEntity
@KomapperTable(schema = "schema", alwaysQuote = true)
data class SchemaOnly(
    @KomapperId val id: Int,
)

@KomapperEntity
@KomapperTable("    ", alwaysQuote = true)
data class BlankName(
    @KomapperId @KomapperColumn("    ", alwaysQuote = true) val id: Int,
)

@KomapperEntity
@KomapperTable(alwaysQuote = true)
data class Order(
    @KomapperId @KomapperColumn(alwaysQuote = true) val orderId: Int,
    @KomapperColumn(alwaysQuote = true) val value: String
)
