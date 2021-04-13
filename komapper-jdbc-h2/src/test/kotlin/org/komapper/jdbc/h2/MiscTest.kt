package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.annotation.KmTable
import org.komapper.annotation.KmVersion
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
        val sql = query.dryRun()
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
        val sql = query.dryRun()
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
        val sql = query.dryRun()
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
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "BLANK_NAME" """))
    }
}
