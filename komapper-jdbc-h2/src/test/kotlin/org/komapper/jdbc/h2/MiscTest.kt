package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.komapper.core.KmColumn
import org.komapper.core.KmEntity
import org.komapper.core.KmId
import org.komapper.core.KmTable
import org.komapper.core.KmVersion
import org.komapper.core.dsl.EntityQuery

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
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."CATALOG_AND_SCHEMA" """))
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
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."CATALOG_ONLY" """))
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
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "schema"."SCHEMA_ONLY" """))
    }

    @KmEntity
    @KmTable("    ")
    data class BlankName(
        @KmId val id: Int,
    )

    @Test
    fun emptyTable() {
        val m = MiscTest_BlankName_()
        val query = EntityQuery.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "BLANK_NAME" """))
    }
}
