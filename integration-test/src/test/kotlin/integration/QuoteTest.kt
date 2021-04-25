package integration

import integration.setting.Dbms
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class QuoteTest(val db: Database) {

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogAndSchema() {
        val m = CatalogAndSchema.alias
        val query = EntityDsl.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."CATALOG_AND_SCHEMA" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogOnly() {
        val m = CatalogOnly.alias
        val query = EntityDsl.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "catalog"."CATALOG_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun schemaOnly() {
        val m = SchemaOnly.alias
        val query = EntityDsl.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "schema"."SCHEMA_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun blankName() {
        val m = BlankName.alias
        val query = EntityDsl.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "BLANK_NAME" """))
        assertTrue(sql.contains("ID"))
    }

    @Test
    fun alwaysQuote() {
        val m = Order.alias
        db.runQuery { EntityDsl.insert(m, Order(1, "value")) }
        val list = db.runQuery { EntityDsl.from(m) }
        assertEquals(1, list.size)
    }
}
