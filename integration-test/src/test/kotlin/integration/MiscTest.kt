package integration

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.komapper.core.dsl.EntityQuery

class MiscTest {

    @Test
    fun catalogAndSchema() {
        val m = CatalogAndSchema.alias
        val query = EntityQuery.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."CATALOG_AND_SCHEMA" """))
    }

    @Test
    fun catalogOnly() {
        val m = CatalogOnly.alias
        val query = EntityQuery.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "catalog"."CATALOG_ONLY" """))
    }

    @Test
    fun schemaOnly() {
        val m = SchemaOnly.alias
        val query = EntityQuery.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "schema"."SCHEMA_ONLY" """))
    }

    @Test
    fun blankName() {
        val m = BlankName.alias
        val query = EntityQuery.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "BLANK_NAME" """))
    }
}
