package integration

import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.annotation.KmTable
import org.komapper.annotation.KmVersion

@KmEntity
@KmTable(catalog = "catalog", schema = "schema")
data class CatalogAndSchema(
    @KmId @KmColumn(name = "ADDRESS_ID") val addressId: Int,
    val street: String,
    @KmVersion val version: Int
) {
    companion object
}

@KmEntity
@KmTable(catalog = "catalog")
data class CatalogOnly(
    @KmId val id: Int,
) {
    companion object
}

@KmEntity
@KmTable(schema = "schema")
data class SchemaOnly(
    @KmId val id: Int,
) {
    companion object
}

@KmEntity
@KmTable("    ")
data class BlankName(
    @KmId val id: Int,
) {
    companion object
}
