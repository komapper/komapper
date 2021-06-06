package integration.jdbc

import integration.BlankName
import integration.CatalogAndSchema
import integration.CatalogOnly
import integration.Order
import integration.SchemaOnly
import integration.meta
import integration.setting.Dbms
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.jdbc.Database

@ExtendWith(Env::class)
class QuoteTest(val db: Database) {

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogAndSchema() {
        val m = CatalogAndSchema.meta
        val query = EntityDsl.from(m)
        val sql = db.dryRunQuery { query }
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."CATALOG_AND_SCHEMA" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogOnly() {
        val m = CatalogOnly.meta
        val query = EntityDsl.from(m)
        val sql = db.dryRunQuery { query }
        println(sql)
        assertTrue(sql.contains(""" "catalog"."CATALOG_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun schemaOnly() {
        val m = SchemaOnly.meta
        val query = EntityDsl.from(m)
        val sql = db.dryRunQuery { query }
        println(sql)
        assertTrue(sql.contains(""" "schema"."SCHEMA_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun blankName() {
        val m = BlankName.meta
        val query = EntityDsl.from(m)
        val sql = db.dryRunQuery { query }
        println(sql)
        assertTrue(sql.contains(""" "BLANK_NAME" """))
        assertTrue(sql.contains("ID"))
    }

    @Test
    fun alwaysQuote() {
        val m = Order.meta
        db.runQuery { EntityDsl.insert(m).single(Order(1, "value")) }
        val list = db.runQuery { EntityDsl.from(m) }
        assertEquals(1, list.size)
    }
}
