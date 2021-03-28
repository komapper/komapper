package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.komapper.core.EntityQuery
import org.komapper.core.KmColumn
import org.komapper.core.KmEntity
import org.komapper.core.KmId
import org.komapper.core.KmTable
import org.komapper.core.KmVersion

class MiscTest {

    @KmEntity
    @KmTable(catalog = "catalog", schema = "schema")
    data class CatalogAndSchema(
        @KmId @KmColumn(name = "ADDRESS_ID") val addressId: Int,
        val street: String,
        @KmVersion val version: Int
    )

    @Test
    fun catalogAndSchema() {
        val m = MiscTest_CatalogAndSchema_()
        val query = EntityQuery.from(m)
        val sql = query.peek().sql
        assertTrue(sql.contains("catalog.schema.catalog_and_schema"))
    }

    @KmEntity
    @KmTable(catalog = "catalog")
    data class CatalogOnly(
        @KmId val id: Int,
    )

    @Test
    fun catalogOnly() {
        val m = MiscTest_CatalogOnly_()
        val query = EntityQuery.from(m)
        val sql = query.peek().sql
        assertTrue(sql.contains("catalog.catalog_only"))
    }

    @KmEntity
    @KmTable(schema = "schema")
    data class SchemaOnly(
        @KmId val id: Int,
    )

    @Test
    fun schemaOnly() {
        val m = MiscTest_SchemaOnly_()
        val query = EntityQuery.from(m)
        val sql = query.peek().sql
        assertTrue(sql.contains("schema.schema_only"))
    }
}
